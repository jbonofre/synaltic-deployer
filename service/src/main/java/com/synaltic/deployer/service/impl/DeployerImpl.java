package com.synaltic.deployer.service.impl;

import com.google.common.io.Files;
import com.synaltic.deployer.api.*;
import org.apache.karaf.features.internal.model.*;
import org.apache.karaf.features.internal.model.Bundle;
import org.apache.karaf.features.internal.model.Config;
import org.apache.karaf.features.internal.model.Feature;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class DeployerImpl implements Deployer {

    private final static Logger LOGGER = LoggerFactory.getLogger(DeployerImpl.class);

    private static final Pattern mvnPattern = Pattern.compile("mvn:([^/ ]+)/([^/ ]+)/([^/ ]*)(/([^/ ]+)(/([^/ ]+))?)?");

    public void explodeKar(String artifactUrl, String repositoryUrl) throws Exception {
        File tempDirectory = Files.createTempDir();
        extract(artifactUrl, tempDirectory);
        // upload kar content
        // browse kar inner repository
        File karRepository = new File(tempDirectory, "repository");
        browseKar(karRepository, karRepository.getPath(), repositoryUrl);
    }

    protected void browseKar(File entry, String basePath, String repositoryUrl) {
        if (entry.isDirectory()) {
            File[] files = entry.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    browseKar(file, basePath, repositoryUrl);
                } else {
                    String path = file.getParentFile().getParentFile().getParentFile().getPath();
                    if (path.startsWith(basePath)) {
                        path = path.substring(basePath.length() + 1);
                    }
                    path = path.replace('/', '.');
                    String groupId = path;
                    String artifactId = file.getParentFile().getParentFile().getName();
                    String version = file.getParentFile().getName();
                    String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                    try {
                        uploadArtifact(groupId, artifactId, version, extension, file, repositoryUrl);
                    } catch (Exception e) {
                        LOGGER.warn("Can't deploy artifact {}/{}/{}/{}", new String[]{ groupId, artifactId, version, extension }, e);
                    }
                }
            }
        }
    }

    protected static boolean isMavenUrl(String url) {
        Matcher m = mvnPattern.matcher(url);
        return m.matches();
    }

    protected static Map<String, String> parse(String url) {
        Matcher m = mvnPattern.matcher(url);
        if (!m.matches()) {
            return null;
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("groupId", m.group(1));
        result.put("artifactId", m.group(2));
        result.put("version", m.group(3));
        if (m.group(5) == null) {
            result.put("extension", "jar");
        } else {
            result.put("extension", m.group(5));
        }
        result.put("classifier", m.group(7));
        return result;
    }

    public void extract(String url, File baseDir) throws Exception {
        InputStream is = null;
        JarInputStream zipIs = null;

        File repoDir = new File(baseDir, "repository");
        File resourceDir = new File(baseDir, "resource");

        try {
            is = new URI(url).toURL().openStream();
            repoDir.mkdirs();

            zipIs = new JarInputStream(is);
            boolean scanForRepos = true;

            ZipEntry entry = zipIs.getNextEntry();
            while (entry != null) {
                if (entry.getName().startsWith("repository")) {
                    String path = entry.getName().substring("repository/".length());
                    File destFile = new File(repoDir, path);
                    extract(zipIs, entry, destFile);
                }

                if (entry.getName().startsWith("resource")) {
                    String path = entry.getName().substring("resource/".length());
                    File destFile = new File(resourceDir, path);
                    extract(zipIs, entry, destFile);
                }
                entry = zipIs.getNextEntry();
            }
        } finally {
            if (zipIs != null) {
                zipIs.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    private static File extract(InputStream is, ZipEntry zipEntry, File dest) throws Exception {
        if (zipEntry.isDirectory()) {
            dest.mkdirs();
        } else {
            dest.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(dest);
            copyStream(is, out);
            out.close();
        }
        return dest;
    }

    static long copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[10000];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public void uploadArtifact(String groupId,
                String artifactId,
                String version,
                String artifactUrl,
                String repositoryUrl) throws Exception {

        Map<String, String> coordonates = new HashMap<String, String>();
        if (isMavenUrl(artifactUrl)) {
            coordonates = parse(artifactUrl);
        } else {
            int index = artifactUrl.lastIndexOf('.');
            if (index != -1) {
                coordonates.put("extension", artifactUrl.substring(index + 1));
            } else {
                coordonates.put("extension", "jar");
            }
        }

        File artifactFile = File.createTempFile(artifactId, coordonates.get("extension"));

        FileOutputStream os = new FileOutputStream(artifactFile);
        copyStream(new URI(artifactUrl).toURL().openStream(), os);
        os.flush();
        os.close();

        uploadArtifact(groupId, artifactId, version, coordonates.get("extension"), artifactFile, repositoryUrl);
    }

    protected void uploadArtifact(String groupId, String artifactId, String version, String extension, File artifactFile, String repositoryUrl) throws Exception {
        uploadArtifact(groupId, artifactId, version, extension, null, artifactFile, repositoryUrl);
    }

    protected void uploadArtifact(String groupId, String artifactId, String version, String extension, String classifier, File artifactFile, String repositoryUrl) throws Exception {
        DefaultServiceLocator defaultServiceLocator = MavenRepositorySystemUtils.newServiceLocator();
        defaultServiceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        defaultServiceLocator.addService(TransporterFactory.class, FileTransporterFactory.class);
        defaultServiceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        RepositorySystem repositorySystem = defaultServiceLocator.getService(RepositorySystem.class);

        DefaultRepositorySystemSession repositorySystemSession = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(repositorySystemSession, localRepository);
        repositorySystemSession.setLocalRepositoryManager(localRepositoryManager);
        repositorySystemSession.setTransferListener(new ConsoleTransferListener(System.out));
        repositorySystemSession.setRepositoryListener(new ConsoleRepositoryListener(System.out));

        RemoteRepository remoteRepository = new RemoteRepository.Builder("sdeployer", "default", repositoryUrl).build();

        Artifact artifact;
        if (classifier != null) {
            artifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version);
        } else {
            artifact = new DefaultArtifact(groupId, artifactId, extension, version);
        }
        artifact = artifact.setFile(artifactFile);

        InstallRequest installRequest = new InstallRequest();
        installRequest.addArtifact(artifact);
        repositorySystem.install(repositorySystemSession, installRequest);

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact(artifact);
        deployRequest.setRepository(remoteRepository);
        repositorySystem.deploy(repositorySystemSession, deployRequest);
    }

    public void assembleFeature(String groupId,
                  String artifactId,
                  String version,
                  String repositoryUrl,
                  String feature,
                  List<String> featuresRepositoryUrls,
                  List<String> features,
                  List<String> bundles,
                  List<com.synaltic.deployer.api.Config> configs) throws Exception {
        Features featuresModel = new Features();
        featuresModel.setName(feature);
        // add features repository
        if (featuresRepositoryUrls != null) {
            for (String featuresRepositoryUrl : featuresRepositoryUrls) {
                featuresModel.getRepository().add(featuresRepositoryUrl);
            }
        }
        // add wrap feature
        Feature wrapFeature = new Feature();
        wrapFeature.setName(feature);
        wrapFeature.setVersion(version);
        // add inner features
        if (features != null) {
            for (String innerFeature : features) {
                Dependency dependency = new Dependency();
                dependency.setName(innerFeature);
                wrapFeature.getFeature().add(dependency);
            }
        }
        // add bundles
        if (bundles != null) {
            for (String innerBundle : bundles) {
                Bundle bundle = new Bundle();
                bundle.setLocation(innerBundle);
                wrapFeature.getBundle().add(bundle);
            }
        }
        // add config
        if (configs != null) {
            for (com.synaltic.deployer.api.Config config : configs) {
                Config modelConfig = new Config();
                modelConfig.setName(config.getPid());
                StringBuilder builder = new StringBuilder();
                if (config.getProperties() != null) {
                    for (String key : config.getProperties().keySet()) {
                        builder.append(key).append("=").append(config.getProperties().get(key)).append('\n');
                    }
                }
                modelConfig.setValue(builder.toString());
                wrapFeature.getConfig().add(modelConfig);
            }
        }

        featuresModel.getFeature().add(wrapFeature);

        File featuresFile = File.createTempFile(artifactId, "xml");

        FileOutputStream os = new FileOutputStream(featuresFile);

        JaxbUtil.marshal(featuresModel, os);

        uploadArtifact(groupId, artifactId, version, "xml", "features", featuresFile, repositoryUrl);
    }

    public void deployKar(String artifactUrl, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=kar,name=" + karafName);
            connection.invoke(name, "install", new Object[]{ artifactUrl }, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void undeployKar(String id, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=kar,name=" + karafName);
            connection.invoke(name, "uninstall", new Object[]{id}, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public List<String> listKars(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=kar,name=" + karafName);
            return ((List<String>) connection.getAttribute(name, "Kars"));
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void deployBundle(String artifactUrl, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + karafName);
            connection.invoke(name, "install", new Object[]{ artifactUrl, true }, new String[]{ "java.lang.String", boolean.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void undeployBundle(String id, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + karafName);
            connection.invoke(name, "uninstall", new Object[]{id}, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void startBundle(String id, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + karafName);
            connection.invoke(name, "start", new Object[]{id}, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void stopBundle(String id, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + karafName);
            connection.invoke(name, "stop", new Object[]{id}, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public List<com.synaltic.deployer.api.Bundle> listBundles(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + karafName);
            TabularData tabularData = (TabularData) connection.getAttribute(name, "Bundles");
            List<com.synaltic.deployer.api.Bundle> result = new ArrayList<com.synaltic.deployer.api.Bundle>();
            for (Object value : tabularData.values()) {
                CompositeData compositeData = (CompositeData) value;
                Long bundleId = (Long) compositeData.get("ID");
                String bundleName = (String) compositeData.get("Name");
                String bundleVersion = (String) compositeData.get("Version");
                String bundleState = (String) compositeData.get("State");
                com.synaltic.deployer.api.Bundle bundle = new com.synaltic.deployer.api.Bundle();
                bundle.setId(bundleId.toString());
                bundle.setName(bundleName);
                bundle.setVersion(bundleVersion);
                bundle.setState(bundleState);
                result.add(bundle);
            }
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void deployFeaturesRepository(String artifactUrl, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            connection.invoke(name, "addRepository", new Object[]{ artifactUrl, false }, new String[]{ "java.lang.String", boolean.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void undeployFeaturesRepository(String artifactUrl, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            connection.invoke(name, "removeRepository", new Object[]{ artifactUrl, true }, new String[]{ "java.lang.String", boolean.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public List<FeatureRepository> listFeaturesRepositories(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            List<FeatureRepository> result = new ArrayList<FeatureRepository>();
            TabularData tabularData = (TabularData) connection.getAttribute(name, "Repositories");
            for (Object value : tabularData.values()) {
                CompositeData compositeData = (CompositeData) value;
                String repoName = (String) compositeData.get("Name");
                String repoUri = (String) compositeData.get("Uri");
                FeatureRepository repo = new FeatureRepository();
                repo.setName(repoName);
                repo.setUri(repoUri);
                result.add(repo);
            }
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void installFeature(String feature, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            connection.invoke(name, "installFeature", new Object[]{ feature }, new String[]{ "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void uninstallFeature(String feature, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            connection.invoke(name, "uninstallFeature", new Object[]{ feature }, new String[]{ "java.lang.String", });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public List<com.synaltic.deployer.api.Feature> listFeatures(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            TabularData tabularData = (TabularData) connection.getAttribute(name, "Features");
            List<com.synaltic.deployer.api.Feature> result = new ArrayList<com.synaltic.deployer.api.Feature>();
            for (Object value : tabularData.values()) {
                CompositeData compositeData = (CompositeData) value;
                String featureName = (String) compositeData.get("Name");
                String featureVersion = (String) compositeData.get("Version");
                boolean featureInstalled = (Boolean) compositeData.get("Installed");
                com.synaltic.deployer.api.Feature feature = new com.synaltic.deployer.api.Feature();
                feature.setName(featureName);
                feature.setVersion(featureVersion);
                if (featureInstalled)
                    feature.setState("Installed");
                else feature.setState("Uninstalled");
                result.add(feature);
            }
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public List<String> listInstalledFeatures(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            TabularData tabularData = (TabularData) connection.getAttribute(name, "Features");
            List<String> result = new ArrayList<String>();
            for (Object value : tabularData.values()) {
                CompositeData compositeData = (CompositeData) value;
                String featureName = (String) compositeData.get("Name");
                String featureVersion = (String) compositeData.get("Version");
                boolean featureInstalled = (Boolean) compositeData.get("Installed");
                if (featureInstalled)
                    result.add(featureName + "/" + featureVersion);
            }
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void createConfig(String pid, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + karafName);
            connection.invoke(name, "create", new Object[]{ pid }, new String[]{ String.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void deleteConfig(String pid, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + karafName);
            connection.invoke(name, "delete", new Object[]{ pid }, new String[]{ String.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void setConfigProperty(String pid, String key, String value, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + karafName);
            connection.invoke(name, "setProperty", new Object[]{ pid, key, value }, new String[]{ String.class.getName(), String.class.getName(), String.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public String getConfigProperty(String pid, String key, String jmxUrl, String karafName, String user, String password) throws Exception {
        Map<String, String> properties = this.configProperties(pid, jmxUrl, karafName, user, password);
        return properties.get(key);
    }

    public void deleteConfigProperty(String pid, String key, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + karafName);
            connection.invoke(name, "deleteProperty", new Object[]{ pid, key }, new String[]{ String.class.getName(), String.class.getName()} );
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void appendConfigProperty(String pid, String key, String value, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + karafName);
            connection.invoke(name, "appendProperty", new Object[]{ pid, key, value }, new String[]{ String.class.getName(), String.class.getName(), String.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void updateConfig(com.synaltic.deployer.api.Config config, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + karafName);
            connection.invoke(name, "update", new Object[] { config.getPid(), config.getProperties() }, new String[]{ String.class.getName(), Map.class.getName() });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public Map<String, String> configProperties(String pid, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=config,name=" + karafName);
            Map<String, String> result = (Map<String, String>) connection.invoke(name, "listProperties", new Object[]{ pid }, new String[]{ String.class.getName() });
            return result;
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public List<String> clusterNodes(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        List<String> nodes = new ArrayList<String>();
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=node,name=" + karafName);
            TabularData tabularData = (TabularData) connection.getAttribute(name, "nodes");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String id = (String) data.get("id");
                nodes.add(id);
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return nodes;
    }

    public Map<String, List<String>> clusterGroups(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        Map<String, List<String>> groups = new HashMap<String, List<String>>();
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=group,name=" + karafName);
            TabularData tabularData = (TabularData) connection.getAttribute(name, "groups");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String group = (String) data.get("name");
                String members = (String) data.get("members");
                List<String> m = Arrays.asList(members.split(" "));
                groups.put(group, m);
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return groups;
    }

    public void clusterFeatureInstall(String feature, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + karafName);
            connection.invoke(name, "installFeature", new Object[]{ clusterGroup, feature }, new String[]{ "java.lang.String", "java.lang.String"});
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public boolean isFeatureOnCluster(String feature, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + karafName);
            TabularData tabularData = (TabularData) connection.getAttribute(name, "features");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String featureName = (String) data.get("name");
                boolean installed = (Boolean) data.get("installed");
                if (feature.equals(featureName) && installed) {
                    return true;
                }
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return false;
    }

    public boolean isFeatureLocal(String feature, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            TabularData tabularData = (TabularData) connection.getAttribute(name, "features");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String featureName = (String) data.get("name");
                boolean installed = (Boolean) data.get("installed");
                if (feature.equals(featureName) && installed) {
                    return true;
                }
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return false;
    }

    public void clusterFeatureRepositoryRemove(String id, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + karafName);
            connection.invoke(name, "removeRepository", new Object[]{ clusterGroup, id }, new String[]{ "java.lang.String", "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public boolean isFeatureRepositoryLocal(String id, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=feature,name=" + karafName);
            TabularData tabularData = (TabularData) connection.getAttribute(name, "repositories");
            for (Object value : tabularData.values()) {
                CompositeData data = (CompositeData) value;
                String repoName = (String) data.get("Name");
                String url = (String) data.get("Uri");
                if (repoName.equals(id) || url.equals(id)) {
                    return true;
                }
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return false;
    }

    public void clusterFeatureUninstall(String feature, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + karafName);
            connection.invoke(name, "uninstallFeature", new Object[]{ clusterGroup, feature }, new String[]{ "java.lang.String", "java.lang.String"});
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void clusterFeatureRepositoryAdd(String url, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + karafName);
            connection.invoke(name, "addRepository", new Object[]{ clusterGroup, url }, new String[]{ "java.lang.String", "java.lang.String" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public boolean isFeatureRepositoryOnCluster(String id, String clusterGroup, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf.cellar:type=feature,name=" + karafName);
            List<String> repositories = (List<String>) connection.getAttribute(name, "repositories");
            for (String repository : repositories) {
                if (repository.equals("id")) {
                    return true;
                }
            }
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
        return false;
    }

    private JMXConnector connect(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXServiceURL jmxServiceURL = new JMXServiceURL(jmxUrl);
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        String[] credentials = new String[]{ user, password };
        env.put("jmx.remote.credentials", credentials);
        return JMXConnectorFactory.connect(jmxServiceURL, env);
    }

}

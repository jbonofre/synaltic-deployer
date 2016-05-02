package com.synaltic.deployer.service.impl;

import com.google.common.io.Files;
import com.synaltic.deployer.api.Deployer;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.DefaultArtifactDeployer;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeployerImpl implements Deployer {

    private static final Pattern mvnPattern = Pattern.compile("mvn:([^/ ]+)/([^/ ]+)/([^/ ]*)(/([^/ ]+)(/([^/ ]+))?)?");

    private List<ArtifactRepository> remoteRepos;

    public DeployerImpl() {
        ArtifactRepository central = new DefaultArtifactRepository("http://repo.maven.apache.org/maven2/", "http://repo.maven.apache.org/maven2/", new DefaultRepositoryLayout());
        remoteRepos = new LinkedList<ArtifactRepository>();
    }

    public void explodeKar(String artifactUrl, String repositoryUrl) throws Exception {
        File tempDirectory = Files.createTempDir();
        File karFile = resolveFile(artifactUrl);
        extract(new ZipArchiveInputStream(new FileInputStream(karFile)), tempDirectory);

        ArtifactRepository remoteRepo = new DefaultArtifactRepository(repositoryUrl, repositoryUrl, new DefaultRepositoryLayout());
        ArtifactDeployer deployer = new DefaultArtifactDeployer();
        // deployer.deploy()

    }

    protected static boolean isMavenUrl(String name) {
        Matcher m = mvnPattern.matcher(name);
        return m.matches();
    }

    private File resolveFile(String url) {
        File fileResolved = null;

        if (isMavenUrl(url)) {
            fileResolved = new File(fromMaven(url));
            ArtifactResolver artifactResolver = new DefaultArtifactResolver();
            try {
                Artifact artifactTemp = resourceToArtifact(url, false);
                if (!fileResolved.exists()) {
                    try {
                        artifactResolver.resolve(artifactTemp, remoteRepos, null);
                        fileResolved = artifactTemp.getFile();
                    } catch (ArtifactResolutionException e) {

                    } catch (ArtifactNotFoundException e) {

                    }
                }
            } catch (Exception e) {

            }
        } else {
            fileResolved = new File(url);
        }

        return fileResolved;
    }

    /**
     * Convert a feature resourceLocation (bundle or configuration file) into an artifact.
     *
     * @param resourceLocation the feature resource location (bundle or configuration file).
     * @param skipNonMavenProtocols flag to skip protocol different than mvn:
     * @return the artifact corresponding to the resource.
     */
    protected Artifact resourceToArtifact(String resourceLocation, boolean skipNonMavenProtocols) throws Exception {
        resourceLocation = resourceLocation.replace("\r\n", "").replace("\n", "").replace(" ", "").replace("\t", "");
        final int index = resourceLocation.indexOf("mvn:");
        if (index < 0) {
            if (skipNonMavenProtocols) {
                return null;
            }
            throw new IllegalArgumentException("Resource URL is not a Maven URL: " + resourceLocation);
        } else {
            resourceLocation = resourceLocation.substring(index + "mvn:".length());
        }
        // Truncate the URL when a '#', a '?' or a '$' is encountered
        final int index1 = resourceLocation.indexOf('?');
        final int index2 = resourceLocation.indexOf('#');
        int endIndex = -1;
        if (index1 > 0) {
            if (index2 > 0) {
                endIndex = Math.min(index1, index2);
            } else {
                endIndex = index1;
            }
        } else if (index2 > 0) {
            endIndex = index2;
        }
        if (endIndex >= 0) {
            resourceLocation = resourceLocation.substring(0, endIndex);
        }
        final int index3 = resourceLocation.indexOf('$');
        if (index3 > 0) {
            resourceLocation = resourceLocation.substring(0, index3);
        }

        //check if the resourceLocation descriptor contains also remote repository information.
        ArtifactRepository repo = null;
        if (resourceLocation.startsWith("http://")) {
            final int repoDelimIntex = resourceLocation.indexOf('!');
            String repoUrl = resourceLocation.substring(0, repoDelimIntex);

            repo = new DefaultArtifactRepository(
                    repoUrl,
                    repoUrl,
                    new DefaultRepositoryLayout());
            resourceLocation = resourceLocation.substring(repoDelimIntex + 1);

        }
        String[] parts = resourceLocation.split("/");
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = null;
        String classifier = null;
        String type = "jar";
        if (parts.length > 2) {
            version = parts[2];
            if (parts.length > 3) {
                type = parts[3];
                if (parts.length > 4) {
                    classifier = parts[4];
                }
            }
        }
        ArtifactFactory factory = new DefaultArtifactFactory();
        Artifact artifact = null;
        System.out.println("GroupId: " + groupId);
        System.out.println("ArtifactId: " + artifactId);
        System.out.println("Version: " + version);
        System.out.println("Type: " + type);
        System.out.println("Classifier: " + classifier);
        if (classifier == null) {
            artifact = new DefaultArtifact(groupId, artifactId, version, "compile", type, "", null);
            // artifact = factory.createProjectArtifact(groupId, artifactId, version, type);
        } else {
            artifact = factory.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
        }
        artifact.setRepository(repo);
        return artifact;
    }


    /**
         * Return a path for an artifact:
         * - if the input is already a path (doesn't contain ':'), the same path is returned.
         * - if the input is a Maven URL, the input is converted to a default repository location path, type and classifier
         *   are optional.
         *
         * @param name artifact data
         * @return path as supplied or a default Maven repository path
         */
    private static String fromMaven(String name) {
        Matcher m = mvnPattern.matcher(name);
        if (!m.matches()) {
            return name;
        }

        StringBuilder b = new StringBuilder();
        b.append(m.group(1));
        for (int i = 0; i < b.length(); i++) {
            if (b.charAt(i) == '.') {
                b.setCharAt(i, '/');
            }
        }
        b.append("/"); // groupId
        String artifactId = m.group(2);
        String version = m.group(3);
        String extension = m.group(5);
        String classifier = m.group(7);
        b.append(artifactId).append("/"); // artifactId
        b.append(version).append("/"); // version
        b.append(artifactId).append("-").append(version);
        if (present(classifier)) {
            b.append("-").append(classifier);
        }
        if (present(classifier)) {
            b.append(".").append(extension);
        } else {
            b.append(".jar");
        }
        return b.toString();
    }

    private static boolean present(String part) {
        return part != null && !part.isEmpty();
    }

    private static void extract(ArchiveInputStream is, File targetDir) throws IOException {
        try {
            targetDir.mkdirs();
            ArchiveEntry entry = is.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                name = name.substring(name.indexOf("/") + 1);
                File file = new File(targetDir, name);
                if (entry.isDirectory()) {
                    file.mkdirs();
                }
                else {
                    file.getParentFile().mkdirs();
                    OutputStream os = new FileOutputStream(file);
                    try {
                        IOUtils.copy(is, os);
                    }
                    finally {
                        IOUtils.closeQuietly(os);
                    }
                }
                entry = is.getNextEntry();
            }
        }
        finally {
            is.close();
        }
    }

    public void uploadArtifact(String groupId,
                String artifactId,
                String version,
                String artifactUrl,
                String repositoryUrl) throws Exception {

        PlexusContainer container = new DefaultPlexusContainer();

        ArtifactRepositoryFactory repositoryFactory = (ArtifactRepositoryFactory) container.lookup(ArtifactRepositoryFactory.ROLE);

        ArtifactFactory artifactFactory = (ArtifactFactory) container.lookup(ArtifactFactory.ROLE);

        ProjectBuilder projectBuilder = container.lookup(ProjectBuilder.class);

        RepositorySystem repositorySystem = container.lookup(RepositorySystem.class);

        ArtifactRepository localRepository = repositorySystem.createDefaultLocalRepository();
        ArtifactRepository remoteRepository = repositorySystem.createArtifactRepository("remote", repositoryUrl, new DefaultRepositoryLayout(), new ArtifactRepositoryPolicy(), new ArtifactRepositoryPolicy());

        Artifact artifact = artifactFactory.createProjectArtifact(groupId, artifactId, version);

        ArtifactDeployer artifactDeployer = new DefaultArtifactDeployer();
        File artifactFile = resolveFile(artifactUrl);
        artifact.setFile(artifactFile);
        artifactDeployer.deploy(artifactFile, artifact, remoteRepository, localRepository);
    }

    public void assembleFeature(String groupId,
                  String artifactId,
                  String version,
                  String repositoryUrl,
                  String feature,
                  List<String> featuresRepositoryUrls,
                  List<String> features) {
        // TODO
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

    public void undeployKar(String karName, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=kar,name=" + karafName);
            connection.invoke(name, "uninstall", new Object[]{ karName }, new String[]{ "java.lang.String" });
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
            connection.invoke(name, "install", new Object[]{ artifactUrl, Boolean.TRUE }, new String[]{ "java.lang.String", "java.lang.Boolean" });
        } finally {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        }
    }

    public void undeployBundle(String artifactUrl, String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXConnector jmxConnector = connect(jmxUrl, karafName, user, password);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("org.apache.karaf:type=bundle,name=" + karafName);
            connection.invoke(name, "uninstall", new Object[]{ artifactUrl }, new String[]{ "java.lang.String" });
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
            connection.invoke(name, "addRepository", new Object[]{ artifactUrl, Boolean.TRUE }, new String[]{ "java.lang.String", "java.lang.Boolean" });
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
            connection.invoke(name, "removeRepository", new Object[]{ artifactUrl, Boolean.TRUE }, new String[]{ "java.lang.String", "java.lang.Boolean" });
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

    private JMXConnector connect(String jmxUrl, String karafName, String user, String password) throws Exception {
        JMXServiceURL jmxServiceURL = new JMXServiceURL(jmxUrl);
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        String[] credentials = new String[]{ user, password };
        env.put("jmx.remote.credentials", credentials);
        return JMXConnectorFactory.connect(jmxServiceURL, env);
    }

}

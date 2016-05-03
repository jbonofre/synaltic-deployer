package com.synaltic.deployer.service.impl;

import com.google.common.io.Files;
import com.synaltic.deployer.api.Deployer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.ArtifactUpload;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.SubArtifact;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

public class DeployerImpl implements Deployer {

    public void explodeKar(String artifactUrl, String repositoryUrl) throws Exception {
        File tempDirectory = Files.createTempDir();
        extract(artifactUrl, tempDirectory);
        // upload kar content
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

        // File artifactFile = File.createTempFile(artifactId, "jar");
        File artifactFile = new File("/tmp/test.jar");

        FileOutputStream os = new FileOutputStream(artifactFile);
        copyStream(new URI(artifactUrl).toURL().openStream(), os);
        os.flush();
        os.close();


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

        Artifact artifact = new DefaultArtifact(groupId, artifactId, "jar", version);
        artifact = artifact.setFile(artifactFile);

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

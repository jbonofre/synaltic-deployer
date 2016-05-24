package com.synaltic.deployer.rest;

import com.synaltic.deployer.api.Deployer;
import com.synaltic.deployer.service.impl.DeployerImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

public class DeployerRestTest {

    private DeployerRest rest;

    @Before
    public void init() throws Exception {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.io.tmpdir", "target");
        Deployer deployer = new DeployerImpl();
        rest = new DeployerRest();
        rest.setDeployer(deployer);
    }

    @Test
    public void explodeKarTest() throws Exception {
        System.out.println("This test is step 1 in the use case:");
        System.out.println("\t- User creates a kar locally");
        System.out.println("\t- The kar is uploaded on Maven repo (using mvn deploy:deploy-file or API");
        System.out.println("\t- The kar is exploded on the Maven repo");

        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setRepositoryUrl("file:target/test/repository");
        uploadRequest.setArtifactUrl("file:src/test/resources/test.kar");
        uploadRequest.setGroupId("kar-test");
        uploadRequest.setArtifactId("kar-test");
        uploadRequest.setVersion("1.0-SNAPSHOT");
        rest.uploadArtifact(uploadRequest);

        KarExplodeRequest karExplodeRequest = new KarExplodeRequest();
        // TODO: the artifact URL should be the one on Maven repository
        // To simplify we use test resources location directly
        karExplodeRequest.setArtifactUrl("file:src/test/resources/test.kar");
        karExplodeRequest.setRepositoryUrl("file:target/test/repository");
        rest.explodeKar(karExplodeRequest);
    }

    @Test
    @Ignore
    public void installFeatureTest() throws Exception {
        System.out.println("This test is the following step in the use case (if user doesn't want to create an uber feature)");
        System.out.println("WARNING: This test requires a running Karaf instance");
        System.out.println("- It registers a features repository");
        System.out.println("- Then installs a feature");

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setArtifactUrl("mvn:org.apache.camel.karaf/apache-camel/2.17.1/xml/features");
        deployRequest.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        deployRequest.setKarafName("root");
        deployRequest.setUser("karaf");
        deployRequest.setPassword("karaf");
        rest.deployFeaturesRepository(deployRequest);

        deployRequest = new DeployRequest();
        deployRequest.setArtifactUrl("camel-blueprint");
        deployRequest.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        deployRequest.setKarafName("root");
        deployRequest.setUser("karaf");
        deployRequest.setPassword("karaf");
        rest.installFeature(deployRequest);
    }

    @Test
    @Ignore
    public void assembleFeatureTest() throws Exception {
        System.out.println("This test is a second step in the use case.");
        System.out.println("The user creates a \"meta\" feature, assembling existing feature");
        System.out.println("Then he can install this \"meta\" feature");

        FeatureAssembleRequest assembleRequest = new FeatureAssembleRequest();
        assembleRequest.setVersion("1.0-SNAPSHOT");
        assembleRequest.setArtifactId("business-feature");
        assembleRequest.setFeature("business-feature");
        assembleRequest.setGroupId("business-feature");
        assembleRequest.setRepositoryUrl("file:" + System.getProperty("user.home") + "/.m2/repository");
        assembleRequest.setFeatureRepositories(Arrays.asList(new String[]{ "mvn:org.apache.camel.karaf/apache-camel/2.17.1/xml/features" }));
        assembleRequest.setFeatures(Arrays.asList(new String[]{ "camel-blueprint", "camel-stream" }));
        rest.assembleFeature(assembleRequest);

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setArtifactUrl("mvn:business-feature/business-feature/1.0-SNAPSHOT/xml/features");
        deployRequest.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        deployRequest.setKarafName("root");
        deployRequest.setUser("karaf");
        deployRequest.setPassword("karaf");
        rest.deployFeaturesRepository(deployRequest);

        deployRequest = new DeployRequest();
        deployRequest.setArtifactUrl("business-feature");
        deployRequest.setJmxUrl("service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root");
        deployRequest.setKarafName("root");
        deployRequest.setUser("karaf");
        deployRequest.setPassword("karaf");
        rest.installFeature(deployRequest);
    }

}

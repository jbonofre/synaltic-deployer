package com.synaltic.deployer.service.impl;

import com.synaltic.deployer.api.Deployer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeployerImplTest {

    private Deployer deployer;

    @Before
    public void startup() {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.io.tmpdir", "target");
        deployer = new DeployerImpl();
    }

    @Test
    public void explodeKarTest() throws Exception {
        deployer.explodeKar("mvn:org.apache.karaf.features/framework/4.0.4/kar", "file:target/test/repository/kar");
    }

    @Test
    public void uploadArtifactTest() throws Exception {
        deployer.uploadArtifact("test", "test", "1.0-SNAPSHOT", "mvn:commons-lang/commons-lang/2.6", "file:target/test/repository");
    }

    @Test
    public void assembleFeatureTest() throws Exception {
        List<String> featureRepositories = new ArrayList<String>();
        featureRepositories.add("mvn:org.apache.camel.karaf/apache-camel/2.17.2/xml/features");
        List<String> features = new ArrayList<String>();
        features.add("camel-spring");
        features.add("camel-jms");
        features.add("camel-stream");
        List<String> bundles = new ArrayList<String>();
        bundles.add("mvn:commons-lang/commons-lang/2.6");
        deployer.assembleFeature("test-feature", "test-feature", "1.0-SNAPSHOT", "file:target/test/repository", "test-feature", featureRepositories, features, bundles);
    }

    @Test
    public void assembleFeatureWithNullTest() throws Exception {
       deployer.assembleFeature("null-feature", "null-feature", "1.0-SNAPSHOT", "file:target/test/repository", "null-feature", null, null, null);
    }

    @Test
    public void mvnParseTest() throws Exception {
        String mvnUrl = "mvn:testGroupId/testArtifactId/1.0";
        Map<String, String> coordonates = DeployerImpl.parse(mvnUrl);
        Assert.assertEquals("testGroupId", coordonates.get("groupId"));
        Assert.assertEquals("testArtifactId", coordonates.get("artifactId"));
        Assert.assertEquals("1.0", coordonates.get("version"));
        Assert.assertEquals("jar", coordonates.get("extension"));
        Assert.assertNull(coordonates.get("classifier"));

        mvnUrl = "mvn:testGroupId/testArtifactId/1.0/kar";
        coordonates = DeployerImpl.parse(mvnUrl);
        Assert.assertEquals("testGroupId", coordonates.get("groupId"));
        Assert.assertEquals("testArtifactId", coordonates.get("artifactId"));
        Assert.assertEquals("1.0", coordonates.get("version"));
        Assert.assertEquals("kar", coordonates.get("extension"));
        Assert.assertNull(coordonates.get("classifier"));

        mvnUrl = "mvn:testGroupId/testArtifactId/1.0/xml/features";
        coordonates = DeployerImpl.parse(mvnUrl);
        Assert.assertEquals("testGroupId", coordonates.get("groupId"));
        Assert.assertEquals("testArtifactId", coordonates.get("artifactId"));
        Assert.assertEquals("1.0", coordonates.get("version"));
        Assert.assertEquals("xml", coordonates.get("extension"));
        Assert.assertEquals("features", coordonates.get("classifier"));
    }

}

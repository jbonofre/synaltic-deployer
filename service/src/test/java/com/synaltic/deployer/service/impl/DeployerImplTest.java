package com.synaltic.deployer.service.impl;

import com.synaltic.deployer.api.Deployer;
import org.junit.Before;
import org.junit.Test;

public class DeployerImplTest {

    private Deployer deployer;

    @Before
    public void startup() {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.io.tmpdir", "target");
        deployer = new DeployerImpl();
    }

    @Test
    public void explodeKar() throws Exception {
        deployer.explodeKar("mvn:org.apache.karaf.features/framework/4.0.4/kar", null);
    }

    @Test
    public void uploadArtifactTest() throws Exception {
        deployer.uploadArtifact("test", "test", "1.0-SNAPSHOT", "mvn:commons-lang/commons-lang/2.6", "file:target/test/repository");
    }

}

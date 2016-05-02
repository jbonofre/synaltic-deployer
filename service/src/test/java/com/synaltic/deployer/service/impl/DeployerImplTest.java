package com.synaltic.deployer.service.impl;

import org.junit.Test;

public class DeployerImplTest {

    @Test
    public void uploadArtifactTest() throws Exception {
        DeployerImpl deployer = new DeployerImpl();
        deployer.uploadArtifact("test", "test", "1.0-SNAPSHOT", "mvn:commons-lang/commons-lang/2.6", "http://maven.nanthrax.net");
    }

}

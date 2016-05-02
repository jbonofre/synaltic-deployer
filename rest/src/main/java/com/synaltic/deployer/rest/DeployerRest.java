package com.synaltic.deployer.rest;

import com.synaltic.deployer.api.Deployer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/sdeployer")
public class DeployerRest {

    private Deployer deployer;

    @Path("/artifact/upload")
    @Consumes("application/json")
    @POST
    public void uploadArtifact(UploadRequest request) {
        // TODO
    }

    @Path("/deploy/bundle")
    @Consumes("application/json")
    @POST
    public void deployBundle(DeployRequest request) throws Exception {
        deployer.deployBundle(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/deploy/kar")
    @Consumes("application/json")
    @POST
    public void deployKar(DeployRequest request) throws Exception {
        deployer.deployKar(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/deploy/feature")
    @Consumes("application/json")
    @POST
    public void deployFeaturesRepository(DeployRequest request) throws Exception {
        deployer.deployFeaturesRepository(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/install/feature")
    @Consumes("application/json")
    @POST
    public void installFeature(DeployRequest request) throws Exception {
        deployer.installFeature(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }
}

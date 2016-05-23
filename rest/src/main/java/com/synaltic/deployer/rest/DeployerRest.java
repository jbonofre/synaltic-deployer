package com.synaltic.deployer.rest;

import com.synaltic.deployer.api.Deployer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/sdeployer")
public class DeployerRest {

    private Deployer deployer;

    @Path("/kar/explode")
    @Consumes("application/json")
    @POST
    public void explodeKar(KarExplodeRequest request) throws Exception {
        deployer.explodeKar(request.getArtifactUrl(), request.getRepositoryUrl());
    }

    @Path("/artifact/upload")
    @Consumes("application/json")
    @POST
    public void uploadArtifact(UploadRequest request) throws Exception {
        deployer.uploadArtifact(request.getGroupId(),
                request.getArtifactId(),
                request.getVersion(),
                request.getArtifactUrl(),
                request.getRepositoryUrl());
    }

    @Path("/bundle/deploy")
    @Consumes("application/json")
    @POST
    public void deployBundle(DeployRequest request) throws Exception {
        deployer.deployBundle(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/bundle/undeploy")
    @Consumes("application/json")
    @POST
    public void undeployBundle(DeployRequest request) throws Exception {
        deployer.undeployBundle(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/kar/deploy")
    @Consumes("application/json")
    @POST
    public void deployKar(DeployRequest request) throws Exception {
        deployer.deployKar(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/kar/undeploy")
    @Consumes("application/json")
    @POST
    public void undeployKar(DeployRequest request) throws Exception {
        deployer.undeployKar(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/feature/assemble")
    @Consumes("application/json")
    @POST
    public void assembleFeature(FeatureAssembleRequest request) throws Exception {
        deployer.assembleFeature(request.getGroupId(),
                request.getArtifactId(),
                request.getVersion(),
                request.getRepositoryUrl(),
                request.getFeature(),
                request.getFeatureRepositories(),
                request.getFeatures());
    }

    @Path("/feature/deploy")
    @Consumes("application/json")
    @POST
    public void deployFeaturesRepository(DeployRequest request) throws Exception {
        deployer.deployFeaturesRepository(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/feature/undeploy")
    @Consumes("application/json")
    @POST
    public void undeployFeaturesRepository(DeployRequest request) throws Exception {
        deployer.undeployFeaturesRepository(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/feature/install")
    @Consumes("application/json")
    @POST
    public void installFeature(DeployRequest request) throws Exception {
        deployer.installFeature(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/feature/uninstall")
    @Consumes("application/json")
    @POST
    public void uninstallFeature(DeployRequest request) throws Exception {
        deployer.uninstallFeature(request.getArtifactUrl(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

}

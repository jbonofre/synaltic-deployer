package com.synaltic.deployer.rest;

import com.synaltic.deployer.api.Deployer;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;

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
                request.getFeatures(),
                request.getBundles(),
                request.getConfigs());
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

    @Path("/config/create")
    @Consumes("application/json")
    @POST
    public void createConfig(ConfigRequest request) throws Exception {
        deployer.createConfig(request.getPid(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/config/delete")
    @Consumes("application/json")
    @DELETE
    public void deleteConfig(ConfigRequest request) throws Exception {
        deployer.deleteConfig(request.getPid(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/config/properties")
    @Consumes("application/json")
    @Produces("application/json")
    @GET
    public Map<String, String> configProperties(ConfigRequest request) throws Exception {
        return deployer.configProperties(request.getPid(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/config/update")
    @Consumes("application/json")
    @POST
    public void updateConfig(ConfigUpdateRequest request) throws Exception {
        deployer.updateConfig(request.getConfig(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/config/property/set")
    @Consumes("application/json")
    @POST
    public void setConfigProperty(ConfigPropertyRequest request) throws Exception {
        deployer.setConfigProperty(request.getPid(),
                request.getKey(),
                request.getValue(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/config/property/get")
    @Consumes("application/json")
    @GET
    public String getConfigProperty(ConfigPropertyKeyRequest request) throws Exception {
        return deployer.getConfigProperty(request.getPid(),
                request.getKey(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/config/property/delete")
    @Consumes("application/json")
    @DELETE
    public void deleteConfigProperty(ConfigPropertyKeyRequest request) throws Exception {
        deployer.deleteConfigProperty(request.getPid(),
                request.getKey(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/config/property/append")
    @Consumes("application/json")
    @POST
    public void appendConfigProperty(ConfigPropertyRequest request) throws Exception {
        deployer.appendConfigProperty(request.getPid(),
                request.getKey(),
                request.getValue(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/cluster/feature/deploy")
    @Consumes("application/json")
    @POST
    public void clusterDeployFeaturesRepository(ClusterDeployRequest request) throws Exception {
        deployer.clusterFeatureRepositoryAdd(request.getArtifactUrl(),
                request.getClusterGroup(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/cluster/feature/undeploy")
    @Consumes("application/json")
    @POST
    public void clusterUndeployFeaturesRepository(ClusterDeployRequest request) throws Exception {
        deployer.clusterFeatureRepositoryRemove(request.getArtifactUrl(),
                request.getClusterGroup(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/cluster/feature/install")
    @Consumes("application/json")
    @POST
    public void clusterInstallFeature(ClusterDeployRequest request) throws Exception {
        deployer.clusterFeatureInstall(request.getArtifactUrl(),
                request.getClusterGroup(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/cluster/feature/uninstall")
    @Consumes("application/json")
    @POST
    public void clusterUninstallFeature(ClusterDeployRequest request) throws Exception {
        deployer.clusterFeatureUninstall(request.getArtifactUrl(),
                request.getClusterGroup(),
                request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/cluster/nodes")
    @Consumes("application/json")
    @Produces("application/json")
    @POST
    public List<String> clusterNodes(DeployRequest request) throws Exception {
        return deployer.clusterNodes(request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    @Path("/cluster/groups")
    @Consumes("application/json")
    @Produces("application/json")
    @POST
    public Map<String, List<String>> clusterGroups(DeployRequest request) throws Exception {
        return deployer.clusterGroups(request.getJmxUrl(),
                request.getKarafName(),
                request.getUser(),
                request.getPassword());
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

}

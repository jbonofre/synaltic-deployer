package com.synaltic.deployer.rest;

import com.synaltic.deployer.api.Deployer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Component
@Path("/sdeployer")
public class DeployerRest {

    @Reference
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

}

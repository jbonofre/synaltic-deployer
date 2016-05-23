package com.synaltic.deployer.rest;

public class ClusterDeployRequest extends DeployRequest {

    private String clusterGroup;

    public String getClusterGroup() {
        return clusterGroup;
    }

    public void setClusterGroup(String clusterGroup) {
        this.clusterGroup = clusterGroup;
    }

}

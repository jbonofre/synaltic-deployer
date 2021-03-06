package com.synaltic.deployer.rest;

import com.synaltic.deployer.api.Config;

import java.util.List;

public class FeatureAssembleRequest {

    private String groupId;
    private String artifactId;
    private String version;
    private String feature;
    private String repositoryUrl;
    private List<String> featureRepositories;
    private List<String> features;
    private List<String> bundles;
    private List<Config> configs;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public List<String> getFeatureRepositories() {
        return featureRepositories;
    }

    public void setFeatureRepositories(List<String> featureRepositories) {
        this.featureRepositories = featureRepositories;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getBundles() {
        return bundles;
    }

    public void setBundles(List<String> bundles) {
        this.bundles = bundles;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }

}

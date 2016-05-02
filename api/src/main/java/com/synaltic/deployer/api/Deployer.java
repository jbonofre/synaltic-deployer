package com.synaltic.deployer.api;

import java.util.List;

public interface Deployer {

    /**
     * Explode a kar file on a Maven repository.
     *
     * @param artifactUrl the artifact URL.
     * @param repositoryUrl the Maven repository URL.
     * @throws Exception in case of extraction failure.
     */
    void explodeKar(String artifactUrl,
                 String repositoryUrl) throws Exception;

    /**
     * Upload an artifact on a Maven repository.
     *
     * @param groupId the target groupId.
     * @param artifactId the target artifactId.
     * @param version the target version.
     * @param artifactUrl the bundle URL.
     * @param repositoryUrl the Maven repository URL.
     * @throws Exception in case of upload failure.
     */
    void uploadArtifact(String groupId,
                String artifactId,
                String version,
                String artifactUrl,
                String repositoryUrl) throws Exception;

    /**
     * Assemble a feature based on existing features, and create the corresponding feature repository.
     *
     * @param groupId The target feature repository groupId.
     * @param artifactId The target feature repository artifactId.
     * @param version The target feature and feature repository version.
     * @param repositoryUrl The Maven repository URL.
     * @param feature The name of the target feature.
     * @param featuresRepositoryUrls The list of existing features repository URLs to use.
     * @param features The list of existing features to include in the assembly feature.
     */
    void assembleFeature(String groupId,
                  String artifactId,
                  String version,
                  String repositoryUrl,
                  String feature,
                  List<String> featuresRepositoryUrls,
                  List<String> features);

    /**
     * Deploy a bundle to a given Karaf instance.
     *
     * @param artifactUrl The artifact URL.
     * @param jmxUrl The JMX URL of the the Karaf instance.
     * @param karafName The name of the target Karaf instance on the JMX URL (root for Karaf vanilla, tesb for Talend ESB Runtime).
     * @param user The JMX MBeanServer username.
     * @param password The JMX MBeanServer password.
     * @throws Exception If the deployment fails.
     */
    void deployBundle(String artifactUrl,
                String jmxUrl,
                String karafName,
                String user,
                String password) throws Exception;

    void undeployBundle(String artifactUrl,
                        String jmxUrl,
                        String karafName,
                        String user,
                        String password) throws Exception;

    void deployKar(String artifactUrl,
                   String jmxUrl,
                   String karafName,
                   String user,
                   String password) throws Exception;

    void undeployKar(String artifactUrl,
                     String jmxUrl,
                     String karafName,
                     String user,
                     String password) throws Exception;

    void deployFeaturesRepository(String artfifactUrl,
                                  String jmxUrl,
                                  String karafName,
                                  String user,
                                  String password) throws Exception;

    void undeployFeaturesRepository(String artifactUrl,
                                    String jmxUrl,
                                    String karafName,
                                    String user,
                                    String password) throws Exception;

    void installFeature(String feature,
                        String jmxUrl,
                        String karafName,
                        String user,
                        String password) throws Exception;

    void uninstallFeature(String feature,
                          String jmxUrl,
                          String karafName,
                          String user,
                          String password) throws Exception;

}

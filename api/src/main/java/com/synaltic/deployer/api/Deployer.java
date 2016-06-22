package com.synaltic.deployer.api;

import java.util.List;
import java.util.Map;

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
     * @param bundles The list of bundles URLs to include in the assembly feature.
     * @param configs The list of configurations to include in the assembly feature.
     */
    void assembleFeature(String groupId,
                  String artifactId,
                  String version,
                  String repositoryUrl,
                  String feature,
                  List<String> featuresRepositoryUrls,
                  List<String> features,
                  List<String> bundles,
                  List<Config> configs) throws Exception;

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

    /**
     * Undeploy a bundle from a given Karaf instance.
     *
     * @param id The bundle ID.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of undeployment failure.
     */
    void undeployBundle(String id,
                        String jmxUrl,
                        String karafName,
                        String user,
                        String password) throws Exception;

    /**
     * List the bundles.
     *
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return The list of installed bundles URLs.
     * @throws Exception
     */
    List<String> listBundles(String jmxUrl,
                             String karafName,
                             String user,
                             String password) throws Exception;

    /**
     * Deploy a KAR to a given Karaf instance.
     *
     * @param artifactUrl The KAR location URL.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of KAR deployment failure.
     */
    void deployKar(String artifactUrl,
                   String jmxUrl,
                   String karafName,
                   String user,
                   String password) throws Exception;

    /**
     * Undeploy a KAR from a given Karaf instance.
     *
     * @param id The KAR name.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of KAR undeploy failure.
     */
    void undeployKar(String id,
                     String jmxUrl,
                     String karafName,
                     String user,
                     String password) throws Exception;

    /**
     * List the KARs.
     *
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return The list of KARs.
     * @throws Exception
     */
    List<String> listKars(String jmxUrl,
                          String karafName,
                          String user,
                          String password) throws Exception;

    /**
     * Add a features repository in a given Karaf instance.
     *
     * @param artfifactUrl The features repository URL.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of features repository adding failure.
     */
    void deployFeaturesRepository(String artfifactUrl,
                                  String jmxUrl,
                                  String karafName,
                                  String user,
                                  String password) throws Exception;

    /**
     * Remove a features repository from a given Karaf instance.
     *
     * @param artifactUrl The features repository URL or name.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of features repository removing failure.
     */
    void undeployFeaturesRepository(String artifactUrl,
                                    String jmxUrl,
                                    String karafName,
                                    String user,
                                    String password) throws Exception;

    /**
     * List the features repositories URL.
     *
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return The list of features repositories.
     * @throws Exception In case of failure.
     */
    List<String> listFeaturesRepositories(String jmxUrl,
                                          String karafName,
                                          String user,
                                          String password) throws Exception;

    /**
     * List all features.
     *
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return The list of features (installed or global).
     * @throws Exception
     */
    List<String> listFeatures(String jmxUrl,
                              String karafName,
                              String user,
                              String password) throws Exception;

    /**
     * List all installed features.
     *
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return The list of features (installed or global).
     * @throws Exception
     */
    List<String> listInstalledFeatures(String jmxUrl,
                              String karafName,
                              String user,
                              String password) throws Exception;

    /**
     * Install a feature to a given Karaf instance.
     *
     * @param feature The feature name (name/version).
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of installat failure.
     */
    void installFeature(String feature,
                        String jmxUrl,
                        String karafName,
                        String user,
                        String password) throws Exception;

    /**
     * Uninstall a feature from a given Karaf instance.
     *
     * @param feature The feature name (name/version).
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of uninstall failure.
     */
    void uninstallFeature(String feature,
                          String jmxUrl,
                          String karafName,
                          String user,
                          String password) throws Exception;

    /**
     * Create an empty configuration on the given Karaf instance.
     *
     * @param pid The configuration PID to create.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of create failure.
     */
    void createConfig(String pid,
                      String jmxUrl,
                      String karafName,
                      String user,
                      String password) throws Exception;

    /**
     * Get the properties contained in a configuration on the given Karaf instance.
     *
     * @param pid The configuration PID.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return the key,value pairs in the configuration.
     * @throws Exception In case of retrieval failure.
     */
    Map<String, String> configProperties(String pid,
                                         String jmxUrl,
                                         String karafName,
                                         String user,
                                         String password) throws Exception;

    /**
     * Create or update a configuration in a given Karaf instance.
     *
     * @param config The configuration to update.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of configuration update failure.
     */
    void updateConfig(Config config,
                      String jmxUrl,
                      String karafName,
                      String user,
                      String password) throws Exception;

    /**
     * Delete a configuration from a given Karaf instance.
     *
     * @param pid The configuration PID.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of configuration delete failure.
     */
    void deleteConfig(String pid,
                      String jmxUrl,
                      String karafName,
                      String user,
                      String password) throws Exception;

    /**
     * Append a value at the end of the given property value on the given Karaf instance.
     *
     * @param pid The configuration PID.
     * @param key The configuration property key.
     * @param value The configuration property value.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of append failure.
     */
    void appendConfigProperty(String pid, String key, String value,
                              String jmxUrl,
                              String karafName,
                              String user,
                              String password) throws Exception;

    /**
     * Overwrite a value of the given property on the given Karaf instance.
     *
     * @param pid The configuration PID.
     * @param key The configuration property key.
     * @param value The configuration property value.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of set failure.
     */
    void setConfigProperty(String pid, String key, String value,
                           String jmxUrl,
                           String karafName,
                           String user,
                           String password) throws Exception;

    /**
     * Get the value of a config property on the given Karaf instance.
     *
     * @param pid The configuration PID.
     * @param key The configuration property key.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return The value of config property.
     * @throws Exception In case of get failure.
     */
    String getConfigProperty(String pid, String key,
                             String jmxUrl,
                             String karafName,
                             String user,
                             String password) throws Exception;

    /**
     * Delete a config property in the given Karaf instance.
     *
     * @param pid The configuration PID.
     * @param key The configuration property key.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of delete failure.
     */
    void deleteConfigProperty(String pid, String key,
                              String jmxUrl,
                              String karafName,
                              String user,
                              String password) throws Exception;

    /**
     * List the nodes in the cluster.
     *
     * @param jmxUrl The Karaf MBean server JMX URL where to perform the Cellar action.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean password.
     * @return The list of cluster node IDs.
     * @throws Exception In case of nodes lookup failure.
     */
    List<String> clusterNodes(String jmxUrl,
                      String karafName,
                      String user,
                      String password) throws Exception;

    /**
     * List the groups in the cluster.
     *
     * @param jmxUrl The Karaf MBean server JMX URL where to perform the Cellar action.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean password.
     * @return The list of groups in the cluster with the members.
     * @throws Exception In case of groups lookup failure.
     */
    Map<String, List<String>> clusterGroups(String jmxUrl,
                                            String karafName,
                                            String user,
                                            String password) throws Exception;

    /**
     * Add a features repository in a cluster group.
     *
     * @param repositoryUrl The features repository location URL.
     * @param clusterGroup The target cluster group.
     * @param jmxUrl The Karaf MBean server JMX URL where to perform the Cellar action.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of add failure.
     */
    void clusterFeatureRepositoryAdd(String repositoryUrl,
                                     String clusterGroup,
                                     String jmxUrl,
                                     String karafName,
                                     String user,
                                     String password) throws Exception;

    /**
     * Remove a features repository from a cluster group.
     *
     * @param repositoryId The features repository ID.
     * @param clusterGroup The target cluster group.
     * @param jmxUrl The Karaf MBean server JMX URL where to perform the Cellar action.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of remove failure.
     */
    void clusterFeatureRepositoryRemove(String repositoryId,
                                        String clusterGroup,
                                        String jmxUrl,
                                        String karafName,
                                        String user,
                                        String password) throws Exception;

    /**
     * Check if a features repository is present on a cluster group.
     *
     * @param repositoryUrl The features repository URL.
     * @param clusterGroup The cluster group.
     * @param jmxUrl The Karaf MBean server JMX URL where to perform the Cellar action.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return True if the features repository is present in the cluster group, false else.
     * @throws Exception In case of check failure.
     */
    boolean isFeatureRepositoryOnCluster(String repositoryUrl,
                                         String clusterGroup,
                                         String jmxUrl,
                                         String karafName,
                                         String user,
                                         String password) throws Exception;

    /**
     * Check if a features repository is present on the given instance (node).
     *
     * @param repositoryUrl The features repository URL.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return True if the features repository is present on the target instance (node), false else.
     * @throws Exception In case of check failure.
     */
    boolean isFeatureRepositoryLocal(String repositoryUrl,
                                     String jmxUrl,
                                     String karafName,
                                     String user,
                                     String password) throws Exception;

    /**
     * Install a feature on a cluster group.
     *
     * @param feature The feature to install.
     * @param clusterGroup The target cluster group.
     * @param jmxUrl The Karaf MBean server JMX URL where to perform the Cellar action.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of install failure.
     */
    void clusterFeatureInstall(String feature,
                                  String clusterGroup,
                                  String jmxUrl,
                                  String karafName,
                                  String user,
                                  String password) throws Exception;

    /**
     * Uninstall a feature from a cluster group.
     *
     * @param feature The feature to install.
     * @param clusterGroup The cluster group.
     * @param jmxUrl The Karaf MBean server JMX URL where to perform the Cellar action.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @throws Exception In case of uninstall failure.
     */
    void clusterFeatureUninstall(String feature,
                                 String clusterGroup,
                                 String jmxUrl,
                                 String karafName,
                                 String user,
                                 String password) throws Exception;

    /**
     * Check if a feature is present (installed) on a cluster group.
     *
     * @param feature The feature to check.
     * @param clusterGroup The cluster group.
     * @param jmxUrl The Karaf MBean server JMX URL where to perform the Cellar action.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return True if the feature is present (installed) on the cluster group, false else.
     * @throws Exception In case of check failure.
     */
    boolean isFeatureOnCluster(String feature,
                               String clusterGroup,
                               String jmxUrl,
                               String karafName,
                               String user,
                               String password) throws Exception;

    /**
     * Check if a feature is present (installed) on a Karaf instance (node).
     *
     * @param feature The feature to check.
     * @param jmxUrl The Karaf MBean server JMX URL.
     * @param karafName The Karaf instance name.
     * @param user The Karaf MBean server username.
     * @param password The Karaf MBean server password.
     * @return True if the feature is present (installed) on the instance, false else.
     * @throws Exception In case of check failure.
     */
    boolean isFeatureLocal(String feature,
                           String jmxUrl,
                           String karafName,
                           String user,
                           String password) throws Exception;

}

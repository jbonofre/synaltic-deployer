Once you cloned the project, you can build with:

----
mvn clean install
----

You can bypass the test:

----
mvn clean install -DskipTests
----

To install in Karaf runtime:

----
karaf@root()> feature:repo-add mvn:com.synaltic.deployer/features/1.0-SNAPSHOT/xml/features
karaf@root()> feature:install synaltic-deployer-rest
----
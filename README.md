Once you cloned the project, you can build with:

----
mvn clean install
----

You can bypass the test:

----
mvn clean install -DskipTests
----

To install in Talend ESB runtime (avoiding refresh and extender issue):

----
karaf@trun()> bundle:install -s mvn:com.synaltic.deployer/api/1.0-SNAPSHOT
karaf@trun()> bundle:install -s mvn:org.apache.httpcomponents/httpcore-osgi/4.2.5
karaf@trun()> bundle:install -s mvn:org.apache.httpcomponents/httpclient-osgi/4.2.5
karaf@trun()> bundle:install -s mvn:com.synaltic.deployer/service/1.0-SNAPSHOT
karaf@trun()> bundle:install -s mvn:com.fasterxml.jackson.core/jackson-core/2.4.6
karaf@trun()> bundle:install -s mvn:com.fasterxml.jackson.core/jackson-annotations/2.4.6
karaf@trun()> bundle:install -s mvn:com.fasterxml.jackson.core/jackson-databind/2.4.6
karaf@trun()> bundle:install -s mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-base/2.4.6
karaf@trun()> bundle:install -s mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-json-provider/2.4.6
karaf@trun()> bundle:install -s mvn:com.synaltic.deployer/rest/1.0-SNAPSHOT
----

To install in Karaf vanilla, you can use the provided feature:

----
karaf@root()> feature:repo-add mvn:com.synaltic.deployer/features/1.0-SNAPSHOT/xml/features
karaf@root()> feature:install synaltic-deployer-rest
----

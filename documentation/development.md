Borg Development Guide
======================

Technologies and frameworks used
--------------------------------

* Java 8 (Java 7 for Openshift)
* [JBoss EAP 6.4](http://developers.redhat.com/downloads/) - Java EE 6 - CDI, EJB Session beans, Hibernate JPA
* JSF2, [PrettyFaces](http://ocpsoft.org/prettyfaces/), [Rome Feeds parser](https://rometools.jira.com/wiki/display/ROME/Home)
* [Zurb Foundation 4](http://foundation.zurb.com/docs/v/4.3.2/), [jQuery](http://jquery.com/), [Datatables](http://www.datatables.net/), [Twitter4j](http://twitter4j.org/)
* JUnit for unit tests

How to build
------------

It's necessary to use **Maven 3** to build this project! To build it simply use:

		mvn clean package

Build output is a `borg.war` file placed in the `target` folder.

The `pom.xml` file defines a few [build profiles](http://maven.apache.org/guides/introduction/introduction-to-profiles.html) 
used to build for different target environments (the `localhost` profile is activated by default):

#### localhost 

* build for development and testing in the localhost environment. Very easy deployment to the JBoss EAP 6 `standalone` configuration. 
* [Mysql](http://www.mysql.com/) used for persistence over `java:jboss/datasources/MysqlDS` datasource which is needed to configure in JBoss EAP 6.

#### openshift

* Build for testing deployment on [OpenShift](http://openshift.redhat.com) 
* MySQL OpenShift cartridge provided database for persistence.


How to deploy
-------------

#### localhost development

Create borg database in local Mysql database and user with appropriate access:

		CREATE DATABASE borg;
		CREATE USER borg@localhost;
		GRANT ALL PRIVILEGES ON borg.* TO borg@localhost;

Add Mysql module to JBoss EAP
Create directories `{JBOSS_EAP}/modules/com/mysql/jdbc/main/` containing module.xml and [mysql-connector-java-5.1.38.jar](http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.38/mysql-connector-java-5.1.38.jar):

		module.xml
		mysql-connector-java-5.1.38.jar

Content of `module.xml`:

		<module xmlns="urn:jboss:module:1.0" name="com.mysql.jdbc">
		  <resources>
			<resource-root path="mysql-connector-java-5.1.38.jar"/>
		  </resources>
		  <dependencies>
			<module name="javax.api"/>
		  </dependencies>
		</module>

Add Mysql datasource to JBoss EAP in `{JBOSS_EAP}/standalone/configuration/standalone.xml`

		<datasource jndi-name="java:jboss/datasources/MysqlDS" pool-name="MysqlDS" enabled="true" use-java-context="true">
			<connection-url>jdbc:mysql://localhost:3306/borg</connection-url>
			<driver>mysql</driver>
			<transaction-isolation>TRANSACTION_READ_COMMITTED</transaction-isolation>
			<pool>
				<min-pool-size>10</min-pool-size>
				<max-pool-size>100</max-pool-size>
				<prefill>true</prefill>
			</pool>
			<security>
				<user-name>borg</user-name>
			</security>
			<statement>
				<prepared-statement-cache-size>32</prepared-statement-cache-size>
				<share-prepared-statements>true</share-prepared-statements>
			</statement>
		</datasource>

and driver

		<driver name="mysql" module="com.mysql.jdbc">
			<driver-class>com.mysql.jdbc.Driver</driver-class>
			<xa-datasource-class>com.mysql.jdbc.jdbc2.optional.MysqlXADataSource</xa-datasource-class>
		</driver>

Add Infinispan cache container `borg` with proper caches

        <subsystem xmlns="urn:jboss:domain:infinispan:1.5">
            <cache-container name="borg">
                <local-cache name="sync-feeds"/>
            </cache-container>




Build project with `localhost` development profile. 
Deploy `borg.war` to the JBoss EAP 6 `standalone` configuration, i.e. copy it 
to the `{JBOSS_EAP}/standalone/deployments` folder. 

You can use [Eclipse with JBoss Tools](http://www.jboss.org/tools) or 
[JBoss Developer Studio](https://devstudio.jboss.com) for this as well.

Make sure that enable-welcome-root parameter is set to false in standalone.xml because context of app is mapped to root by default in JBoss AS

        <subsystem xmlns="urn:jboss:domain:web:1.0" default-virtual-server="default-host">
            <connector name="http" protocol="HTTP/1.1" socket-binding="http" scheme="http"/>
            <virtual-server name="default-host" enable-welcome-root="false">
        </subsystem>

The Borg application is then available at [http://localhost:8080/](http://localhost:8080/)

#### Openshift development

1. Create JBoss EAP cartridge with MySQL 
2. Add git remote Openshift repo URL to your local clone named as e.g. openshift
3. If necessary set mysql to be case insensitive by setting this env property via `rhc` command
	
		rhc env set OPENSHIFT_MYSQL_LOWER_CASE_TABLE_NAMES=1 -a planet

4. Do `git push openshift master`


DCP Integration
---------------

Borg using DCP as back-end. Content is identified as [blogpost](https://github.com/jbossorg/dcp-api/blob/master/documentation/rest-api/content/blogpost.md).

During pushing data to DCP data is normalized by DCP input preprocessors especially:

* Feed to Project mapping
* Post author to Contributor mapping

Check out [jboss.org example](https://github.com/jbossorg/dcp-api/blob/master/configuration/data/provider/jbossorg.json) how input processors are configured in jboss.org case.


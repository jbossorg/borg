Borg Development Guide
======================

## Technologies used

* JBoss EAP 6 - Java EE 6 - CDI, EJB Session beans, Hibernate JPA
* JUnit for unit tests

## Build

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


## Deployment

#### localhost development

Create borg database in local Mysql database and user with appropriate access:

		CREATE DATABASE borg;
		CREATE USER borg@localhost;
		GRANT ALL PRIVILEGES ON borg.* TO borg@localhost;

Add Mysql module to JBoss EAP
Create directories `{JBOSS_EAP}/modules/com/mysql/jdbc/main/` containing:

                              module.xml
                              mysql-connector-java-5.1.20-bin.jar

Content of `module.xml`:

		<module xmlns="urn:jboss:module:1.0" name="com.mysql">
		  <resources>
		    <resource-root path="mysql-connector-java-5.1.20-bin.jar"/>
		  </resources>
		  <dependencies>
		    <module name="javax.api"/>
		  </dependencies>
		</module>

Add Mysql datasource to JBoss EAP in `standalone/configuration/standalone.xml`

                <datasource jndi-name="java:jboss/datasources/MysqlDS" pool-name="MysqlDS">
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

Build project with `localhost` development profile. 
Deploy `borg.war` to the JBoss EAP 6 `standalone` configuration, i.e. copy it 
to the `$EAP6HOME/standalone/deployments` folder. 
You can use [Eclipse with JBoss Tools](http://www.jboss.org/tools) or 
[JBoss Developer Studio](https://devstudio.jboss.com) for this.


The Borg application is then available at [http://localhost:8080/borg/](http://localhost:8080/borg/)


# xodus-entity-browser
Web UI entity browser for Xodus database. Provides ability to search, delete, create and edit entities.

## Configuring
Application needs store location path and store access key for proper work: 
* key for location is "xodus.store.location"
* key for store access key is "xodus.store.key"
* key for path to custom properties file is "xodus.store.file.config"

The lookup order for this parameters is:
* System properties - application try to get (location, access key) from System.properties by specified keys.
* Custom configuration file - application get path to custom properties file from system properties by value "xodus
.store.file.config" and try to get values for (location, access key) from it by specified keys.  
* Default configuration file - application gets default properties-file bundled with application 
(src/main/resources/xodus-store.properties) and lookup (location, access key) parameters from it by specified keys.

## Running with Tomcat(7.*)
run from command line:
>mvn clean install

Then deploy war-file from target folder (target/xodus-entity-browser.war) into Tomcat (7.*).
 
NOTE: Do not forget to place correct values into default configuration file (src/main/resources/xodus-store
.properties) or add parameters to Tomcat startup script.  


## Running with maven

run from command line:
>mvn jetty:run-war -Dxodus.store.location=/path/to/store -Dxodus.store.key=storekey





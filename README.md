# xodus-entity-browser
Web UI entity browser for xodus database. Provides ability to search, delete, create and edit entities.

## Configuring
Application needs store location path and store access key: 
 key for location is xodus.store.location
 key for store access key is xodus.store.key
The lookup order for this parameters is   
* System properties. Application try to get values from System.properties by specified keys
* Custom configuration file. Application lookup System.properties for location of properties-file (xodus.store.file
.config) and try to get values from file  
* Default configuration file. Application lookup default location of properties-file bundled with application 
(src/main/resources/xodus-store.properties)

## Running with Tomcat(7.*)
run from command line:
>mvn clean install

to build project. When build successfully finished place war-file (target/xodus-entity-browser.war) into tomcat 
NOTE: in this case do not forget to place correct values into default configuration file (src/main/resources/xodus-store
.properties) 


## Running with maven

run from command line:
>mvn jetty:run-war

NOTE: in this case do not forget to place correct values into default configuration file (src/main/resources/xodus-store
.properties) 





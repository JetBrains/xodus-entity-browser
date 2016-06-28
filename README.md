# xodus-entity-browser
Web UI entity browser for Xodus database. Provides ability to search, delete, create and edit entities.

## Run

* get [latest version](https://bintray.com/artifact/download/lehvolk/maven/com/lehvolk/xodus/entity-browser-launcher/1.0.0-rc5/entity-browser-launcher-1.0.0-rc5.zip)
* execute run.bat or run.sh
* open browser http://localhost:8080 (Jetty server binds to all interfaces on port 8080 therefore all interfaces can be
        used to access application)

## Configuring
* JVM arguments, server port can be modified in startup script.

## Build from sources

* run

    >mvn clean install

    in the root of the project

* unzip file ./entity-browser-launcher/target/xodus-entity-browser-{current.version}.zip
* see Run section to run app
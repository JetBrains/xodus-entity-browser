# xodus-entity-browser
Web UI entity browser for Xodus database. Provides ability to search, delete, create and edit entities.

## Application

Application has 2 builds: based on xodus builds from teamcity.jetbrains.com and based on artifacts from maven repository.

Highly desirable to use entity browser shipped with the same xodus version as used in application which produce database.

That's mean that if your application uses 'x' version of xodus and you modify data with 'y' version of xodus shipped with entity browser then application (with 'x' version of xodus) may not working as expected on this database.

## Run

* get [latest version based on maven repo](https://dl.bintray.com/lehvolk/maven/com/lehvolk/xodus/entity-browser-launcher/1.0.0-20161111/entity-browser-launcher-1.0.0-20161111.zip)
* get [latest version based on teamcity.jetbrains.com](https://dl.bintray.com/lehvolk/maven/com/lehvolk/xodus/entity-browser-launcher/1.0.2395/entity-browser-launcher-1.0.2395.zip)
* execute run.bat or run.sh
* open browser http://localhost:8080 (Jetty server binds to all interfaces on port 8080 therefore all interfaces can be
        used to access application)

## Configuring
* JVM arguments, server port can be modified in startup script.

## Build from sources

* build based on artifacts from teamcity.jetbrains.com

    >./gradlew clean build

Teamcity build number can be changed in /build.gradle via ext.xodusTeamcityBuildNumber value

* build based on maven artifacts

    >./gradlew  -Dxodus-from-maven=true clean build


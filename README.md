# Xodus entity browser

Web UI entity browser for Xodus database. Provides ability to search, delete, create and edit entities. Used in support 
activities for YouTrack and Hub applications.

## Application

Highly desirable to use entity browser shipped with the same Xodus version as used in application which works with database.

## Run

* get [latest build](https://bintray.com/lehvolk/maven/download_file?file_path=com%2Flehvolk%2Fxodus%2Fxodus-entity-browser%2F1.1.0%2Fxodus-entity-browser-1.1.0.zip) and unpack it
* execute run.bat or run.sh
* open browser http://localhost:18080

## Configuring

* JVM arguments, server port can be modified in startup script.

## Build from sources

    >./gradlew clean build

## Run backend

Run method `main` in Main.kt or execute

    >./gradlew runShadow

## Run frontend

Command starts frontend on 19090 port

    >npm install

    >npm start

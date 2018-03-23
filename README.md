# Xodus entity browser
[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Build Status](https://travis-ci.org/JetBrains/xodus-entity-browser.svg?branch=master)](https://travis-ci.org/JetBrains/xodus-entity-browser)

Web UI entity browser for Xodus database stores. Used in support activities for YouTrack and Hub applications. Highly desirable to use entity browser shipped with the same Xodus version as used in application which works with database.

![Databases](https://raw.githubusercontent.com/JetBrains/xodus-entity-browser/master/screenshots/dbs.png)
![List of entities](https://raw.githubusercontent.com/JetBrains/xodus-entity-browser/master/screenshots/entities.png)

## Features

* searching entities with specific query language (search by property values and by links)
* bulk delete operations for search results
* creating new entity types
* edit entities properties and links (blobs are readonly)
* creating and deleting entities
* ability to work with few databases

## Run

* get [latest release](https://github.com/JetBrains/xodus-entity-browser/releases/download/v1.2.0/xodus-entity-browser-1.2.0.zip) and unpack it
* execute run.bat or run.sh
* browser should open automatically. Otherwise goto http://localhost:18080

Custom JVM parameters and custom server port can be specified in startup script.

## Build from sources

    >./gradlew clean build

## Run backend

Run method `main` in Main.kt or execute

    >./gradlew runShadow

## Run frontend

Command starts frontend on 19090 port

    >npm install

    >npm start

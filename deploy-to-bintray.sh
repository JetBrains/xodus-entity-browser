#!/usr/bin/env bash

./gradlew -DBINTRAY_USER=lehvolk -DBINTRAY_API_KEY=d5aa1f2669301d9e6940eb29a8e35a6a07a2bbca clean build bintrayUpload

#!/usr/bin/env bash

./gradlew build
rm -rf build/launcher
unzip build/entity-browser-launcher.zip -d build/launcher/
cd build/launcher
./run.sh
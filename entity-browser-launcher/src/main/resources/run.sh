#!/bin/sh

if [ -z "${JAVA_HOME}" ]; then
  JAVA=java
else
  JAVA="${JAVA_HOME}/bin/java"
  echo "$0 info: Using jdk located in ${JAVA_HOME}."
fi

${JAVA} -Dxodus.store.file.config=./xodus-store.properties -jar jetty-runner.jar --port 8080 ./resources/xodus-entity-browse.war
#!/bin/sh
SERVER_PORT=8080

if [ -z "${JAVA_HOME}" ]; then
  JAVA=java
else
  JAVA="${JAVA_HOME}/bin/java"
  echo "info: Using java located in ${JAVA_HOME}."
fi

${JAVA} -Dxodus.store.file.config=./xodus-store.properties -jar jetty-runner.jar --port ${SERVER_PORT} ./resources/xodus-entity-browse.war
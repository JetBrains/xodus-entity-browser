#!/bin/sh
SERVER_PORT=8080

if [ -z "${JAVA_HOME}" ]; then
  JAVA=java
else
  JAVA="${JAVA_HOME}/bin/java"
  echo "info: Using java located in ${JAVA_HOME}."
fi

${JAVA} -Dlogback.configurationFile=logback.xml -Dserver.port=${SERVER_PORT} -Dentity.browser.config=./xodus-store.properties -Dexodus.entityStore.refactoring.skipAll=true -Dexodus.entityStore.cachingDisabled=true -jar jetty-runner.jar --port ${SERVER_PORT} ./resources/xodus-entity-browse.war
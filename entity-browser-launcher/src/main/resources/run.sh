#!/bin/sh
SERVER_PORT=18080

if [ -z "${JAVA_HOME}" ]; then
  JAVA=java
else
  JAVA="${JAVA_HOME}/bin/java"
  echo "info: Using java located in ${JAVA_HOME}."
fi

${JAVA} \
-Dlogback.configurationFile=logback.xml \
-Dserver.port=${SERVER_PORT} \
-Dexodus.entityStore.refactoring.skipAll=true \
-Dexodus.entityStore.cachingDisabled=true \
-jar xodus-entity-browser.jar
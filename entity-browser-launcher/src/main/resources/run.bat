@echo off
set SERVER_PORT=8080

if "%JAVA_HOME%"=="" (
    set JAVA="java"
) else (
    set JAVA="%JAVA_HOME%\bin\java"
)

%JAVA% -Dlogback.configurationFile=logback.xml -Dserver.port=%SERVER_PORT% -Dentity.browser.config=./xodus-store.properties -Dexodus.entityStore.refactoring.skipAll=true -Dexodus.entityStore.cachingDisabled=true -jar jetty-runner.jar --port %SERVER_PORT% ./resources/xodus-entity-browser.war

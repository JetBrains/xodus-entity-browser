@echo off
set SERVER_PORT=18080

if "%JAVA_HOME%"=="" (
    set JAVA="java"
) else (
    set JAVA="%JAVA_HOME%\bin\java"
)

%JAVA% -Dlogback.configurationFile=logback.xml -Dserver.port=%SERVER_PORT% -Dexodus.entityStore.refactoring.skipAll=true -Dexodus.entityStore.cachingDisabled=true -jar xodus-entity-browser.jar

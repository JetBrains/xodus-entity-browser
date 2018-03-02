@echo off
set SERVER_PORT=18080
set SERVER_HOST=localhost

if "%JAVA_HOME%"=="" (
    set JAVA="java"
) else (
    set JAVA="%JAVA_HOME%\bin\java"
)

%JAVA% -Dlogback.configurationFile=logback.xml -Dserver.port=%SERVER_PORT% -Dserver.host=%SERVER_HOST% -Dexodus.entityStore.refactoring.skipAll=true -Dexodus.entityStore.cachingDisabled=true -jar xodus-entity-browser.jar

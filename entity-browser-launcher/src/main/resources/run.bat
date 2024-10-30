@echo off
set SERVER_PORT=18080
set SERVER_HOST=localhost
set SERVER_CONTEXT=

if "%JAVA_HOME%"=="" (
    set JAVA="java"
) else (
    set JAVA="%JAVA_HOME%\bin\java"
)

%JAVA% -Dlogback.configurationFile=logback.xml ^
-Dserver.port=%SERVER_PORT% ^
-Dserver.host=%SERVER_HOST% ^
-Dserver.context=%SERVER_CONTEXT% ^
-Dexodus.entityStore.refactoring.skipAll=true ^
-Dexodus.entityStore.cachingDisabled=true ^
-jar xodus-entity-browser.jar

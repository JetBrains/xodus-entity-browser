@echo off

if %JAVA_HOME%.==. (
    set JAVA = java
) else (
    set JAVA = %JAVA_HOME%\bin\java
)

%JAVA% -Dxodus.store.file.config=./xodus-store.properties -jar jetty-runner.jar --port 8080 ./resources/xodus-entity-browse.war
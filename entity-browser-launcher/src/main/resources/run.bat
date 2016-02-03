@echo off
set SERVER_PORT=8080

if %JAVA_HOME%.==. (
    set JAVA = java
) else (
    set JAVA = %JAVA_HOME%\bin\java
)

%JAVA% -Dentity.browser.config=./xodus-store.properties -jar jetty-runner.jar --port %SERVER_PORT% ./resources/xodus-entity-browse.war
@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo ============================================================
echo   Starting TaskFlow Web Application & REST API
echo ============================================================
".\maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run

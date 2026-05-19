@echo off
setlocal
set "JAVA_EXE=D:\JDK\jdk-21\bin\java.exe"
set "JAR_FILE=D:\primary_homework_system\back-end\target\primary-homework-backend-0.0.1-SNAPSHOT.jar"

"%JAVA_EXE%" -jar "%JAR_FILE%"

@rem Gradle wrapper script for Windows
@setlocal
@set DIRNAME=%~dp0
@if "%DIRNAME%" == "" set DIRNAME=.
@set APP_HOME=%DIRNAME%
@set GRADLE_USER_HOME=%DIRNAME%.gradle-home
@set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot
@"%JAVA_HOME%\bin\java.exe" -cp "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

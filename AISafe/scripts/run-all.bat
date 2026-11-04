@echo off
REM Builds and starts BOTH replicas of every AISafe service, each one in its own window (Windows).
REM   scripts\run-all.bat                                   -> all 5 services (10 replicas)
REM   scripts\run-all.bat auth-service maintenance-service  -> only these
REM Ports: auth 8051/8052, aircraft 8081/8082, airport 8091/8092, maintenance 8061/8062, route 8083/8084
REM Close a window to stop that replica.
setlocal
set "ROOT=%~dp0.."
if not exist "%ROOT%\certs\aisafe-keystore.p12" (
  echo Certificates not found. Run scripts\gen-certs.bat first.
  exit /b 1
)
set "SERVICES=%*"
if "%SERVICES%"=="" set "SERVICES=auth-service Aircraft_Service airport-service maintenance-service route_flight_service"

for %%S in (%SERVICES%) do (
  echo Building %%S ...
  pushd "%ROOT%\%%S"
  call mvn -q -DskipTests package
  if errorlevel 1 (
    popd
    echo Build of %%S failed. Run "mvn package" inside %%S to see the error.
    exit /b 1
  )
  popd
)

for %%S in (%SERVICES%) do call :startService %%S
echo.
echo Each replica was opened in its own window. Close a window to stop that replica.
exit /b 0

:startService
set "SVC=%~1"
set "JAR="
for %%J in ("%ROOT%\%SVC%\target\*.jar") do set "JAR=%%~nxJ"
if "%JAR%"=="" (
  echo No jar found in %SVC%\target
  exit /b 1
)
start "%SVC%-1" /D "%ROOT%\%SVC%" java -jar "target\%JAR%" --spring.profiles.active=instance1
start "%SVC%-2" /D "%ROOT%\%SVC%" java -jar "target\%JAR%" --spring.profiles.active=instance2
echo Started %SVC% (instance1 and instance2, %JAR%)
exit /b 0

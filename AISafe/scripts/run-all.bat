@echo off
REM Builds and starts BOTH replicas of each service, each one in its own window (Windows).
REM   scripts\run-all.bat                                   -> auth-service airport-service maintenance-service
REM   scripts\run-all.bat auth-service maintenance-service  -> only these
REM Close a window to stop that replica.
setlocal
set ROOT=%~dp0..
if not exist "%ROOT%\certs\aisafe-keystore.p12" (
  echo Certificates not found. Run scripts\gen-certs.bat first.
  exit /b 1
)
set SERVICES=%*
if "%SERVICES%"=="" set SERVICES=auth-service airport-service maintenance-service

for %%S in (%SERVICES%) do (
  echo Building %%S ...
  pushd "%ROOT%\%%S"
  call mvn -q -DskipTests package
  if errorlevel 1 (
    popd
    echo Build of %%S failed
    exit /b 1
  )
  popd
)

for %%S in (%SERVICES%) do (
  start "%%S-1" /D "%ROOT%\%%S" java -jar target\%%S.jar --spring.profiles.active=instance1
  start "%%S-2" /D "%ROOT%\%%S" java -jar target\%%S.jar --spring.profiles.active=instance2
)
echo Each replica was opened in its own window. aircraft-service is started separately.

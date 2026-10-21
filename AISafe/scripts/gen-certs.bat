@echo off
REM Generates the self-signed TLS certificate shared by every AISafe service replica.
REM Output (NOT committed to git): AISafe\certs\aisafe-keystore.p12 and aisafe-truststore.p12
REM Requires: keytool (JDK bin folder in the PATH).
setlocal
set CERTS_DIR=%~dp0..\certs
if "%AISAFE_KEYSTORE_PASSWORD%"=="" (set PASS=changeit) else (set PASS=%AISAFE_KEYSTORE_PASSWORD%)
set SAN=dns:localhost,ip:127.0.0.1,dns:auth-1,dns:auth-2,dns:aircraft-1,dns:aircraft-2,dns:airport-1,dns:airport-2,dns:route-1,dns:route-2,dns:maintenance-1,dns:maintenance-2

if not exist "%CERTS_DIR%" mkdir "%CERTS_DIR%"
del /q "%CERTS_DIR%\aisafe-keystore.p12" "%CERTS_DIR%\aisafe-truststore.p12" "%CERTS_DIR%\aisafe.crt" 2>nul

keytool -genkeypair -alias aisafe -keyalg RSA -keysize 2048 -validity 825 -storetype PKCS12 -keystore "%CERTS_DIR%\aisafe-keystore.p12" -storepass %PASS% -keypass %PASS% -dname "CN=localhost, OU=SIDIS, O=ISEP, C=PT" -ext "SAN=%SAN%" || goto :error
keytool -exportcert -rfc -alias aisafe -keystore "%CERTS_DIR%\aisafe-keystore.p12" -storepass %PASS% -file "%CERTS_DIR%\aisafe.crt" || goto :error
keytool -importcert -noprompt -alias aisafe -file "%CERTS_DIR%\aisafe.crt" -storetype PKCS12 -keystore "%CERTS_DIR%\aisafe-truststore.p12" -storepass %PASS% || goto :error

echo Certificates created in %CERTS_DIR%
exit /b 0
:error
echo Failed to create the certificates (is keytool in the PATH?)
exit /b 1

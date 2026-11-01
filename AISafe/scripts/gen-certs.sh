#!/usr/bin/env bash
# Generates the self-signed TLS certificate shared by every AISafe service replica.
# Output (NOT committed to git): AISafe/certs/aisafe-keystore.p12 and aisafe-truststore.p12
# Requires: keytool (comes with the JDK).
set -euo pipefail

CERTS_DIR="$(cd "$(dirname "$0")/.." && pwd)/certs"
PASS="${AISAFE_KEYSTORE_PASSWORD:-changeit}"
# Names the services use to call each other (localhost and the docker-compose container names)
SAN="dns:localhost,ip:127.0.0.1"
for svc in auth aircraft airport route maintenance; do
  SAN="$SAN,dns:${svc}-1,dns:${svc}-2"
done

mkdir -p "$CERTS_DIR"
rm -f "$CERTS_DIR/aisafe-keystore.p12" "$CERTS_DIR/aisafe-truststore.p12" "$CERTS_DIR/aisafe.crt"

keytool -genkeypair -alias aisafe -keyalg RSA -keysize 2048 -validity 825 \
  -storetype PKCS12 -keystore "$CERTS_DIR/aisafe-keystore.p12" \
  -storepass "$PASS" -keypass "$PASS" \
  -dname "CN=localhost, OU=SIDIS, O=ISEP, C=PT" -ext "SAN=$SAN"

keytool -exportcert -rfc -alias aisafe -keystore "$CERTS_DIR/aisafe-keystore.p12" \
  -storepass "$PASS" -file "$CERTS_DIR/aisafe.crt"

keytool -importcert -noprompt -alias aisafe -file "$CERTS_DIR/aisafe.crt" \
  -storetype PKCS12 -keystore "$CERTS_DIR/aisafe-truststore.p12" -storepass "$PASS"

echo "Certificates created in $CERTS_DIR"

#!/usr/bin/env bash
# Builds and starts BOTH replicas of each service in the background (Mac / Linux).
#   ./scripts/run-all.sh                                  -> auth-service airport-service maintenance-service
#   ./scripts/run-all.sh auth-service maintenance-service -> only these
# Logs:  AISafe/logs/<service>-<replica>.log      Stop: ./scripts/stop-all.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOGS="$ROOT/logs"
mkdir -p "$LOGS"

if [ $# -eq 0 ]; then
  set -- auth-service airport-service maintenance-service
fi

if [ ! -f "$ROOT/certs/aisafe-keystore.p12" ]; then
  echo "Certificates not found. Run ./scripts/gen-certs.sh first."
  exit 1
fi

for svc in "$@"; do
  echo "Building $svc ..."
  (cd "$ROOT/$svc" && mvn -q -DskipTests package)
done

for svc in "$@"; do
  for n in 1 2; do
    (cd "$ROOT/$svc" && nohup java -jar "target/$svc.jar" --spring.profiles.active="instance$n" \
        > "$LOGS/$svc-$n.log" 2>&1 & echo $! >> "$LOGS/pids")
    echo "Started $svc instance$n  (log: logs/$svc-$n.log)"
  done
done

echo
echo "Wait ~20 seconds, then check, e.g.:  curl -k https://localhost:8051/actuator/health"
echo "aircraft-service is started separately (see its own README)."

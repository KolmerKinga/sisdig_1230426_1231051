#!/usr/bin/env bash
# Builds and starts BOTH replicas of every AISafe service in the background (Mac / Linux).
#   ./scripts/run-all.sh                                   -> all 5 services (10 replicas)
#   ./scripts/run-all.sh auth-service maintenance-service  -> only these
# Logs: AISafe/logs/<service>-<replica>.log      Stop everything: ./scripts/stop-all.sh
#
#   service               replica 1   replica 2   protocol
#   auth-service          8051        8052        HTTPS
#   Aircraft_Service      8081        8082        HTTP
#   airport-service       8091        8092        HTTPS
#   maintenance-service   8061        8062        HTTPS
#   route_flight_service  8083        8084        HTTP
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOGS="$ROOT/logs"
mkdir -p "$LOGS"

if [ $# -eq 0 ]; then
  set -- auth-service Aircraft_Service airport-service maintenance-service route_flight_service
fi

ports_of() {
  case "$1" in
    auth-service)         echo "8051 8052" ;;
    Aircraft_Service)     echo "8081 8082" ;;
    airport-service)      echo "8091 8092" ;;
    maintenance-service)  echo "8061 8062" ;;
    route_flight_service) echo "8083 8084" ;;
    *)                    echo "" ;;
  esac
}

if [ ! -f "$ROOT/certs/aisafe-keystore.p12" ]; then
  echo "Certificates not found. Run ./scripts/gen-certs.sh first."
  exit 1
fi

# 1) build every service first, so a compile error stops before anything starts
for svc in "$@"; do
  if [ ! -f "$ROOT/$svc/pom.xml" ]; then
    echo "Unknown service folder: $svc"; exit 1
  fi
  echo "Building $svc ..."
  if ! (cd "$ROOT/$svc" && mvn -q -DskipTests package); then
    echo "Build of $svc FAILED. Run 'cd $svc && mvn package' to see the error."; exit 1
  fi
done

# 2) start both replicas of each service (the jar name is found in target/, whatever it is called)
for svc in "$@"; do
  jar="$(cd "$ROOT/$svc" && ls target/*.jar 2>/dev/null | grep -v '\.original$' | head -1 || true)"
  if [ -z "$jar" ]; then
    echo "No jar found in $svc/target"; exit 1
  fi
  for n in 1 2; do
    (cd "$ROOT/$svc" && nohup java -jar "$jar" --spring.profiles.active="instance$n" \
        > "$LOGS/$svc-$n.log" 2>&1 & echo $! >> "$LOGS/pids")
    echo "Started $svc instance$n  ($jar, log: logs/$svc-$n.log)"
  done
done

# 3) wait until every replica is listening on its port (max ~90 s)
echo
echo "Waiting for the replicas to start ..."
expected=""
for svc in "$@"; do expected="$expected $(ports_of "$svc")"; done
for i in $(seq 1 45); do
  missing=""
  for p in $expected; do
    lsof -t -iTCP:"$p" -sTCP:LISTEN >/dev/null 2>&1 || missing="$missing $p"
  done
  [ -z "$missing" ] && break
  sleep 2
done

echo
for svc in "$@"; do
  for p in $(ports_of "$svc"); do
    if lsof -t -iTCP:"$p" -sTCP:LISTEN >/dev/null 2>&1; then
      printf "  %-22s %s  UP\n" "$svc" "$p"
    else
      printf "  %-22s %s  NOT RUNNING (see logs/%s-*.log)\n" "$svc" "$p" "$svc"
    fi
  done
done
echo
echo "Stop everything with: ./scripts/stop-all.sh"

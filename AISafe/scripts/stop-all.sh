#!/usr/bin/env bash
# Stops every replica of auth, airport and maintenance (Mac / Linux).
# 1) the processes started by ./scripts/run-all.sh (logs/pids)
# 2) anything still LISTENING on their ports (replicas started by hand, IntelliJ, an old run, ...)
# aircraft-service (8081/8082) is NOT touched.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PIDS="$ROOT/logs/pids"
PORTS="8051 8052 8061 8062 8091 8092"

if [ -f "$PIDS" ]; then
  while read -r pid; do
    kill "$pid" 2>/dev/null && echo "Stopped process $pid"
  done < "$PIDS"
  rm -f "$PIDS"
fi

sleep 2
for port in $PORTS; do
  for pid in $(lsof -t -iTCP:"$port" -sTCP:LISTEN 2>/dev/null); do
    kill "$pid" 2>/dev/null && echo "Stopped process $pid (port $port)"
  done
done

sleep 2
still=""
for port in $PORTS; do
  lsof -t -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1 && still="$still $port"
done
if [ -n "$still" ]; then
  echo "Still running on:$still  ->  kill -9 \$(lsof -t -iTCP:<port> -sTCP:LISTEN)"
else
  echo "All replicas stopped."
fi

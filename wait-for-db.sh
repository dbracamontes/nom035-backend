#!/usr/bin/env bash
set -e

# Wait-for-db script: espera hasta que el host:port acepte conexiones TCP
# Variables (pueden sobreescribirse con env): DB_HOST, DB_PORT, DB_TIMEOUT
host="${DB_HOST:-mysql}"
port="${DB_PORT:-3306}"
timeout=${DB_TIMEOUT:-60}

echo "Waiting for ${host}:${port} (timeout ${timeout}s)..."
count=0
while ! nc -z "$host" "$port"; do
  count=$((count+1))
  if [ "$count" -ge "$timeout" ]; then
    echo "Timeout waiting for ${host}:${port} after ${timeout}s"
    exit 1
  fi
  sleep 1
done

echo "${host}:${port} is available — starting application"
exec java -jar app.jar

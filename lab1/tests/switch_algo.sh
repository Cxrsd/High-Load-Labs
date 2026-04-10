#!/bin/bash
# switch_algo.sh — Switch the active Nginx load balancing algorithm.
#
# Usage:
#   bash tests/switch_algo.sh round-robin
#   bash tests/switch_algo.sh least-conn
#   bash tests/switch_algo.sh ip-hash
#   bash tests/switch_algo.sh random
#   bash tests/switch_algo.sh weighted

ALGO="${1:-round-robin}"
ROOT="$(dirname "$0")/.."
SRC="$ROOT/nginx/configs/${ALGO}.conf"
DST="$ROOT/nginx/nginx.conf"

if [ ! -f "$SRC" ]; then
    echo "Unknown algorithm: $ALGO"
    echo "Available: round-robin  least-conn  ip-hash  random  weighted"
    exit 1
fi

cp "$SRC" "$DST"
docker compose -f "$ROOT/docker-compose.yml" exec nginx nginx -s reload
echo "Switched to: $ALGO"
echo "Verify: curl http://localhost/nginx_status"

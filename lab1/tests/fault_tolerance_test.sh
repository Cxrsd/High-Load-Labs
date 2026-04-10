#!/bin/bash
# fault_tolerance_test.sh — Demonstrate Nginx failover by stopping instances.
# Uses curl instead of ab; no extra tools required.
#
# Nginx parameters in nginx.conf that control this behaviour:
#   max_fails=3      — mark server as down after 3 consecutive failures
#   fail_timeout=30s — keep it marked down for 30 s, then retry

ROOT="$(dirname "$0")/.."
COMPOSE="$ROOT/docker-compose.yml"
URL="http://localhost/?mode=simple"

send() {
    local label=$1 n=${2:-20}
    echo ""
    echo "--- $label ($n requests) ---"
    ok=0; fail=0
    for i in $(seq 1 "$n"); do
        resp=$(curl -s -o /dev/null -w "%{http_code}" "$URL")
        if [ "$resp" = "200" ]; then ok=$((ok+1)); else fail=$((fail+1)); fi
    done
    echo "  OK: $ok   Failed: $fail"
}

echo "=== Fault Tolerance Test ==="

send "Baseline — 3 instances"

echo ""
echo "Stopping app2..."
docker compose -f "$COMPOSE" stop app2

send "app2 down — 2 instances"

echo ""
echo "Stopping app3..."
docker compose -f "$COMPOSE" stop app3

send "app2+app3 down — 1 instance"

echo ""
echo "Recovering app2 and app3..."
docker compose -f "$COMPOSE" start app2 app3
sleep 5

send "After recovery — 3 instances"

echo ""
echo "Done. Confirm distribution with: bash tests/watch_distribution.sh"

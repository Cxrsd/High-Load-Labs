#!/bin/bash
# watch_distribution.sh — Make N sequential requests and show which instance handled each.
#
# Usage:
#   bash tests/watch_distribution.sh            # 30 requests, mode=simple
#   bash tests/watch_distribution.sh 50 mixed   # 50 requests, mode=mixed

COUNT="${1:-30}"
MODE="${2:-simple}"

echo "Sending $COUNT requests (mode=$MODE) through Nginx..."
declare -A hits

for i in $(seq 1 "$COUNT"); do
    resp=$(curl -s "http://localhost/?mode=$MODE")
    inst=$(echo "$resp" | grep -o '"instance":"[^"]*"' | cut -d'"' -f4)
    ms=$(echo   "$resp" | grep -o '"elapsedMs":[0-9.]*' | cut -d: -f2)
    hits[$inst]=$((${hits[$inst]:-0} + 1))
    printf "  %3d → %-8s  %s ms\n" "$i" "$inst" "$ms"
done

echo ""
echo "Distribution:"
for inst in $(echo "${!hits[@]}" | tr ' ' '\n' | sort); do
    printf "  %-10s %d requests\n" "$inst" "${hits[$inst]}"
done

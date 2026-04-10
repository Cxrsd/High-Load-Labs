#!/bin/bash
# run_tests.sh — Optional helper: compare algorithms with Apache Benchmark.
# Requires: ab (apache2-utils)
#
# Primary testing tool for this lab is JMeter — see JMeter scenarios below.
# This script is a quick CLI alternative.
#
# Usage:
#   bash tests/run_tests.sh simple    # lightweight requests
#   bash tests/run_tests.sh mixed     # every 5th request is CPU-heavy

MODE="${1:-simple}"
ROOT="$(dirname "$0")/.."
RESULTS="$(dirname "$0")/results"
mkdir -p "$RESULTS"

if ! command -v ab &>/dev/null; then
    echo "ab not found. Install: apt install apache2-utils"
    echo "For JMeter-based testing see lab report."
    exit 1
fi

run() {
    local algo=$1
    echo ""
    echo "[$algo]"
    bash "$(dirname "$0")/switch_algo.sh" "$algo" >/dev/null
    sleep 1
    ab -n 100 -c 10 "http://localhost/?mode=$MODE" > "$RESULTS/${algo}_${MODE}.txt" 2>&1
    grep -E "Requests per second|Time per request \(mean\)|Failed requests" \
        "$RESULTS/${algo}_${MODE}.txt"
}

echo "Algorithm comparison  mode=$MODE"
echo "100 requests, concurrency 10"

for algo in round-robin least-conn ip-hash random; do
    run "$algo"
done

echo ""
echo "Results saved to: $RESULTS/"
echo ""
printf "%-15s %10s %12s %10s\n" "Algorithm" "RPS" "Mean ms" "Failed"
for algo in round-robin least-conn ip-hash random; do
    f="$RESULTS/${algo}_${MODE}.txt"
    rps=$(grep "Requests per second" "$f" | awk '{print $4}')
    ms=$(grep  "Time per request" "$f" | head -1 | awk '{print $4}')
    fail=$(grep "Failed requests" "$f" | awk '{print $3}')
    printf "%-15s %10s %12s %10s\n" "$algo" "$rps" "$ms" "$fail"
done

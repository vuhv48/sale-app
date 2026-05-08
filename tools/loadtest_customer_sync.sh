#!/usr/bin/env bash
set -euo pipefail

# Load test producer for customer sync topic.
# Example:
#   ./tools/loadtest_customer_sync.sh --count 10000 --bootstrap localhost:9092

BOOTSTRAP_SERVERS="localhost:9092"
TOPIC="app-platform.sales.customer-sync"
COUNT=1000
START_INDEX=1
CODE_PREFIX="C"
NAME_PREFIX="Load Test User"
SLEEP_MS=0

usage() {
  cat <<'EOF'
Usage: loadtest_customer_sync.sh [options]

Options:
  --bootstrap <host:port>   Kafka bootstrap servers (default: localhost:9092)
  --topic <topic>           Topic name (default: app-platform.sales.customer-sync)
  --count <n>               Number of messages to send (default: 1000)
  --start <n>               Start index for customer code (default: 1)
  --code-prefix <prefix>    Customer code prefix (default: C)
  --name-prefix <prefix>    Name prefix (default: Load Test User)
  --sleep-ms <n>            Sleep milliseconds between messages (default: 0)
  -h, --help                Show this help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bootstrap) BOOTSTRAP_SERVERS="${2:-}"; shift 2 ;;
    --topic) TOPIC="${2:-}"; shift 2 ;;
    --count) COUNT="${2:-}"; shift 2 ;;
    --start) START_INDEX="${2:-}"; shift 2 ;;
    --code-prefix) CODE_PREFIX="${2:-}"; shift 2 ;;
    --name-prefix) NAME_PREFIX="${2:-}"; shift 2 ;;
    --sleep-ms) SLEEP_MS="${2:-}"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1"; usage; exit 1 ;;
  esac
done

if ! command -v kafka-console-producer >/dev/null 2>&1; then
  echo "kafka-console-producer not found in PATH."
  echo "Please install Kafka CLI tools or run inside the Kafka container."
  exit 1
fi

echo "Sending ${COUNT} messages to ${TOPIC} via ${BOOTSTRAP_SERVERS}..."

for ((i=0; i<COUNT; i++)); do
  seq=$((START_INDEX + i))
  code=$(printf "%s%06d" "${CODE_PREFIX}" "${seq}")
  now=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  event_id="evt-${seq}"
  payload=$(printf '{"eventId":"%s","customerCode":"%s","name":"%s %d","phone":"09%08d","email":"user%d@test.com","taxCode":"T%d","addressLine":"HCM","active":true,"eventTime":"%s"}' \
    "${event_id}" "${code}" "${NAME_PREFIX}" "${seq}" "${seq}" "${seq}" "${seq}" "${now}")
  # Key by customerCode to keep stable partitioning for a customer.
  printf "%s:%s\n" "${code}" "${payload}"
  if [[ "${SLEEP_MS}" -gt 0 ]]; then
    sleep "$(awk "BEGIN { print ${SLEEP_MS} / 1000 }")"
  fi
done | kafka-console-producer \
  --bootstrap-server "${BOOTSTRAP_SERVERS}" \
  --topic "${TOPIC}" \
  --property "parse.key=true" \
  --property "key.separator=:"

echo "Done."

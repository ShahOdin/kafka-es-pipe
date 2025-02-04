#!/bin/bash
set -euo pipefail

records=(
  '{"key": "session_001", "value": "session_001_1_Firefox"}'
  '{"key": "session_001", "value": "session_001_2_Chrome"}'
  '{"key": "session_002", "value": "session_002_1_Chrome"}'
  '{"key": "session_002", "value": "session_002_2_Safari"}'
  '{"key": "session_003", "value": "session_003_1_Safari"}'
  '{"key": "session_003", "value": "session_003_2_Chrome"}'
  '{"key": "session_003", "value": "session_003_3_Firefox"}'
)

for record in "${records[@]}"; do
  payload=$(jq -n --arg rec "$record" '{records: [$rec | fromjson]}')

  response=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Content-Type: application/vnd.kafka.json.v2+json" \
    --data "$payload" \
    "$KAFKA_REST_PROXY_URL/topics/$TOPIC_NAME")

  if [ "$response" -ne 200 ] && [ "$response" -ne 201 ]; then
    echo "Error: Failed to publish message. HTTP status code: $response"
    echo "Failed Record: $record"
    exit 1
  fi

  echo "Successfully published: $record"
done

echo "All sample data published successfully."
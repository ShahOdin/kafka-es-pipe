#!/bin/bash
set -e

echo "Creating Kafka topic '$TOPIC_NAME'..."
export REPLICATION_FACTOR="1"
export PARTITIONS_COUNT="3"
./create-topic.sh
echo "Kafka topic '$TOPIC_NAME' creation process completed."

echo "Publishing sample data..."
export SCHEMA_PATH=./schema.json
echo "Sample data Publication completed."
./publish-sample-messages.sh
echo "Initialization completed successfully."
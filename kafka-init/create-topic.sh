kafka-topics \
--bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
--topic "$TOPIC_NAME" \
--partitions "$PARTITIONS_COUNT" \
--replication-factor "$REPLICATION_FACTOR" \
--create \
--config retention.ms=120000 \
--config retention.bytes=52428800

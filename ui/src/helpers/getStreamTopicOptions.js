export function getStreamTopicOptions() {
  // Provide the most important Apache Kafka topic config options
  const options = [
    "cleanup.policy",
    "compression.type",
    "delete.retention.ms",
    "flush.messages",
    "flush.ms",
    "max.compaction.lag.ms",
    "max.message.bytes",
    "message.timestamp.type",
    "retention.bytes",
    "retention.ms",
  ];

  return options.map((option) =>
    Object.assign({}, { value: option, label: option })
  );
}

export function isStreamHoldingAvroFormat(stream) {
  const streamHoldsAvroFormat =
    [
      stream.spec.kafka["key.deserializer"],
      stream.spec.kafka["value.deserializer"],
      stream.spec.kafka["key.serializer"],
      stream.spec.kafka["value.serializer"],
    ].filter(
      (className) => className !== undefined && className.includes("Avro")
    ).length > 0;

  return streamHoldsAvroFormat;
}

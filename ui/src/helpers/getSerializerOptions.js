export function getSerializerOptions(stream) {
  let options = [
    "io.datacater.core.serde.JsonSerializer",
    "io.datacater.core.serde.AvroSerializer",
    "org.apache.kafka.common.serialization.StringSerializer",
  ];

  // Add custom value.serializer to list of options
  if (
    stream !== undefined &&
    !options.find((option) => option === stream.spec.kafka["value.serializer"])
  ) {
    options.push(stream.spec.kafka["value.serializer"]);
  }

  // Add custom key.serializer to list of options
  if (
    stream !== undefined &&
    !options.find((option) => option === stream.spec.kafka["key.serializer"])
  ) {
    options.push(stream.spec.kafka["key.serializer"]);
  }

  return options
    .filter((option) => ![undefined, ""].includes(option))
    .map((option) => Object.assign({}, { value: option, label: option }));
}

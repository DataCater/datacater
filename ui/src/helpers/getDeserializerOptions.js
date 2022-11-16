export function getDeserializerOptions(stream) {
  let options = [
    "io.datacater.core.serde.JsonDeserializer",
    "io.datacater.core.serde.AvroDeserializer",
  ];

  // Add custom value.deserializer to list of options
  if (
    stream !== undefined &&
    !options.find(
      (option) => option === stream.spec.kafka["value.deserializer"]
    )
  ) {
    options.push(stream.spec.kafka["value.deserializer"]);
  }

  // Add custom key.deserializer to list of options
  if (
    stream !== undefined &&
    !options.find((option) => option === stream.spec.kafka["key.deserializer"])
  ) {
    options.push(stream.spec.kafka["key.deserializer"]);
  }

  return options
    .filter((option) => ![undefined, ""].includes(option))
    .map((option) => Object.assign({}, { value: option, label: option }));
}

export function getStreamConnectionOptions() {
  // Provide the most important consumer and produce config options
  const options = [
    "acks",
    "auto.commit.interval.ms",
    "auto.offset.reset",
    "batch.size",
    "buffer.memory",
    "client.dns.lookup",
    "client.id",
    "compression.type",
    "connections.max.idle.ms",
    "enable.auto.commit",
    "enable.idempotence",
    "fetch.max.bytes",
    "fetch.min.bytes",
    "group.id",
    "group.instance.id",
    "heartbeat.interval.ms",
    "isolation.level",
    "key.deserializer",
    "key.serializer",
    "linger.ms",
    "max.partition.fetch.bytes",
    "max.poll.interval.ms",
    "max.poll.records",
    "max.request.size",
    "partitioner.class",
    "receive.buffer.bytes",
    "request.timeout.ms",
    "retries",
    "sasl.client.callback.handler.class",
    "sasl.jaas.config",
    "sasl.kerberos.service.name",
    "sasl.login.callback.handler.class",
    "sasl.login.class",
    "sasl.mechanism",
    "sasl.oauthbearer.jwks.endpoint.url",
    "sasl.oauthbearer.token.endpoint.url",
    "security.protocol",
    "session.timeout.ms",
    "ssl.enabled.protocols",
    "ssl.key.password",
    "ssl.keystore.certificate.chain",
    "ssl.keystore.key",
    "ssl.keystore.location",
    "ssl.keystore.password",
    "ssl.keystore.type",
    "ssl.protocol",
    "ssl.provider",
    "ssl.truststore.certificates",
    "ssl.truststore.location",
    "ssl.truststore.password",
    "ssl.truststore.type",
    "value.deserializer",
    "value.serializer",
  ];

  return options.map((option) =>
    Object.assign({}, { value: option, label: option })
  );
}

package io.datacater.core.stream;

import java.util.Optional;
import org.eclipse.microprofile.config.ConfigProvider;

public class StaticConfig {
  private StaticConfig() {}

  static final String DATACATER_PREFIX = "datacater-";
  static final String PARTITION_COUNT = "num.partitions";
  static final String REPLICATION_FACTOR = "replication.factor";
  static final String VALUE_DESERIALIZER_TEXT = "value.deserializer";
  static final String KEY_DESERIALIZER_TEXT = "key.deserializer";
  static final String FALSE = "false";
  static final String RECORD_TAG = "record";
  static final String TOPIC_KEY = "topic";
  static final String CONFIG_KEY = "config";
  static final String UUID_TEXT = "uuid";
  static final String PARTITION_TEXT = "partition";
  static final String OFFSET_TEXT = "offset";
  static final String TIMESTAMP_TEXT = "timestamp";
  static final String MAX_POLL_RECORDS = "max.poll.records";
  static final Long STREAM_AMOUNT_MESSAGE_LIMIT = 100L;

  static class EnvironmentVariables {
    private EnvironmentVariables() {}

    static final Optional<String> DEFAULT_TOPIC_NAME =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.kafka.default-topic-name", String.class);
    static final Integer DEFAULT_NUM_PARTITIONS =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.kafka.default-num-partitions", Integer.class)
            .orElse(3);
    static final Short DEFAULT_REPLICATION_FACTOR =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.kafka.default-replication-factor", Short.class)
            .orElse((short) 1);

    static final Integer KAFKA_API_TIMEOUT_MS =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.kafka.api.timeout.ms", Integer.class)
            .orElse(5000);
  }

  static class LoggerMessages {
    private LoggerMessages() {}

    static final String STREAM_DELETE_NOT_FINISHED_MESSAGE =
        "Stream deletion was called without errors but has not finished yet.";
    static final String STREAM_NOT_FOUND_MESSAGE = "Stream not found.";
    static final String STREAM_NOT_FOUND = "The referenced %s UUID could not be found";
    static final String KAFKA_TOPIC_METADATA_NOT_MAPPED =
        "Kafka topic metadata could not be mapped.";
    static final String KAFKA_TOPIC_METADATA_NOT_MAPPED_INTERRUPTED =
        "Kafka topic metadata could not be mapped. The execution thread was interrupted.";
    static final String KAFKA_BROKER_UNAVAILABLE =
        "Connection to node could not be established. Broker may not be available.";
    static final String THREAD_INTERRUPTED = "Thread got interrupted: ";
    static final String TOPIC_UPDATE_TIMEOUT_FORMATTED =
        "Exceeded timeout of %d ms when updating the config of the Apache Kafka topic.";
    static final String STREAM_NOT_CREATED_METADATA_MISMATCH_FORMATTED =
        "Could not create stream as it already exists on the Cluster and the given metadata doesn't match the actual metadata: %s";
    static final String STREAM_INSPECTED_PARTITIONS_NOT_FOUND =
        "%s was inspected but no partitions be found";
    static final String STREAM_INSPECTED_NOT_FOUND = "%s was inspected but could not be found";
    static final String STREAM_SET_OFFSET =
        "Setting inspection offset for stream: %s and partition: %s to: %s";
    static final String CLOSING_STREAM_CONNECTIONS = "Closing kafka admin and consumer connections";
  }
}

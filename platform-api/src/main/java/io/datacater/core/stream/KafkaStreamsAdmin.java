package io.datacater.core.stream;

import static java.util.stream.Collectors.toMap;
import static org.apache.kafka.common.config.ConfigResource.Type.TOPIC;

import io.datacater.core.exceptions.DatacaterException;
import io.datacater.core.exceptions.KafkaConnectionException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

/**
 * Implementation of an external Apache Kafka topic as a stream.
 *
 * <p>Mandatory connection options: - bootstrap.servers - key.deserializer
 *
 * <p>Optional connection options: - value.deserializer.schema - schema.registry.url
 *
 * <p>Optional config options: - key.format (default: json) - num.partitions (default: 3) -
 * replication.factor (default: 1) - value.format (default: json)
 *
 * <p>You can pass all other official Topic-level configuration options as config values (<a
 * href="https://kafka.apache.org/documentation/#topicconfigs">https://kafka.apache.org/documentation/#topicconfigs</a>).
 */
public class KafkaStreamsAdmin implements StreamService {

  private static final Logger LOGGER = Logger.getLogger(KafkaStreamsAdmin.class);

  private static final String PARTITION_COUNT = "num.partitions";
  private static final String REPLICATION_FACTOR = "replication.factor";
  private final Admin admin;
  private final KafkaConsumer<Object, Object> consumer;

  private static final Optional<String> DEFAULT_TOPIC_NAME =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.kafka.default-topic-name", String.class);
  private static final Integer DEFAULT_NUM_PARTITIONS =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.kafka.default-num-partitions", Integer.class)
          .orElse(3);
  private static final Short DEFAULT_REPLICATION_FACTOR =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.kafka.default-replication-factor", Short.class)
          .orElse((short) 1);

  private static final Integer KAFKA_API_TIMEOUT_MS =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.kafka.api.timeout.ms", Integer.class)
          .orElse(5000);

  private final String name;
  private final Integer partitions;
  private final Short replication;

  private KafkaStreamsAdmin(
      String name,
      Integer partitions,
      Short replicationFactor,
      Admin admin,
      KafkaConsumer<Object, Object> consumer) {
    this.name = name;
    this.partitions = partitions;
    this.replication = replicationFactor;
    this.admin = admin;
    this.consumer = consumer;
  }

  public static KafkaStreamsAdmin from(Stream stream) {
    stream
        .spec()
        .getKafka()
        .putIfAbsent("value.deserializer", io.datacater.core.serde.JsonDeserializer.class);
    stream
        .spec()
        .getKafka()
        .putIfAbsent("key.deserializer", io.datacater.core.serde.JsonDeserializer.class);
    Admin admin = Admin.create(stream.spec().getKafka());
    KafkaConsumer<Object, Object> consumer = new KafkaConsumer<>(stream.spec().getKafka());

    Optional<String> name = Optional.of(stream.name());

    Integer partitions = getPartitions(stream.spec().getTopic());
    Short replicationFactor = getReplicationFactor(stream.spec().getTopic());

    return new KafkaStreamsAdmin(
        name.orElse(getDefaultStreamName()), partitions, replicationFactor, admin, consumer);
  }

  private static int getPartitions(Map<String, Object> topicConfig) {
    if (topicConfig.get(PARTITION_COUNT) instanceof String partitions
        && !topicConfig.get(PARTITION_COUNT).toString().isEmpty()) {
      return Integer.parseInt(partitions);
    }
    return DEFAULT_NUM_PARTITIONS;
  }

  private static Short getReplicationFactor(Map<String, Object> topicConfig) {
    if (topicConfig.get(REPLICATION_FACTOR) instanceof String repFactor
        && !topicConfig.get(PARTITION_COUNT).toString().isEmpty()) {
      return Short.valueOf(repFactor);
    }
    return DEFAULT_REPLICATION_FACTOR;
  }

  void createDefaultChannel() {
    NewTopic newTopic =
        new NewTopic(getDefaultStreamName(), DEFAULT_NUM_PARTITIONS, DEFAULT_REPLICATION_FACTOR);
    admin.createTopics(List.of(newTopic));
  }

  public Boolean isValidConfig(Map<String, String> config) {
    Optional<String> newPartitions = Optional.ofNullable(config.get(PARTITION_COUNT));
    Optional<String> newReplicationFactor = Optional.ofNullable(config.get(REPLICATION_FACTOR));
    if (streamExists()
        && newReplicationFactor.isPresent()
        && !Short.valueOf(newReplicationFactor.get()).equals(this.replication)) {
      return false;
    }
    if (streamExists()
        && newPartitions.isPresent()
        && !Integer.valueOf(newPartitions.get()).equals(this.partitions)) {
      return false;
    }
    return true;
  }

  /**
   * Creates the Topic in the Apache Kafka cluster, as specified in the config and only if it does
   * not yet exist.
   *
   * @return Metadata of the Topic as a Map, where keys are the config names and values are the
   *     config values.
   */
  public StreamSpec apply(StreamSpec spec) {
    if (streamExists()) {
      spec = updateStream(spec);
    } else {
      spec = createStream(spec);
    }

    return spec;
  }

  /**
   * Gets the metadata of the Topic in the Apache Kafka Cluster.
   *
   * @return Metadata of the Topic as a Map, where keys are the config names and values are the
   *     config values.
   */
  public Map<String, String> getMetadata() {
    return admin
        .describeConfigs(List.of(new ConfigResource(TOPIC, this.name)))
        .values()
        .entrySet()
        .stream()
        .flatMap(
            entry -> {
              try {
                return kafkaConfigToMap(entry.getValue().get()).entrySet().stream();
              } catch (ExecutionException e) {
                throw new DatacaterException("Kafka topic metadata could not be mapped.");
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DatacaterException(
                    "Kafka topic metadata could not be mapped. The execution thread was interrupted.");
              }
            })
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Inspect (or retrieve) the most recent events of the Topic.
   *
   * @param limit Number of records to retrieve.
   * @param sampleMethod Mode of operation for the retrieval of records. - Uniform: Distributed
   *     retrieval. Records are polled evenly across partitions. The return amount can vary
   *     depending on the amount of messages in a partition. - Sequenced: Fold-right retrieval.
   *     Messages are polled from one partition. If the partition does not contain the amount
   *     defined in `limit`, the next partition is polled and so on.
   * @return a List<StreamMessage> containing the inspected messages form each topic
   */
  public List<StreamMessage> inspect(Stream stream, long limit, SampleMethod sampleMethod) {
    List<StreamMessage> messageList = new ArrayList<>();
    if (!streamExists()) {
      return messageList;
    }
    List<TopicPartition> partitionsList = getPartitions(consumer.partitionsFor(this.name));
    if (partitionsList.isEmpty()) {
      return messageList;
    }
    consumer.assign(partitionsList);

    if (sampleMethod == SampleMethod.UNIFORM) {
      final long partitionMessageAmount =
          (long) Math.ceil((double) limit / (double) partitionsList.size());
      setPartitionOffsets(partitionsList, partitionMessageAmount);
    } else {
      setPartitionOffsets(partitionsList, limit);
    }

    ConsumerRecords<Object, Object> consumerRecords =
        consumer.poll(Duration.ofMillis(KAFKA_API_TIMEOUT_MS));

    consumerRecords.forEach(item -> messageList.add(recordToStreamMessage(item)));
    return messageList.stream().limit(limit).toList();
  }

  private List<TopicPartition> getPartitions(List<PartitionInfo> allPartitions) {
    List<TopicPartition> partitionsList = new ArrayList<>();
    for (PartitionInfo partitionInfo : allPartitions) {
      TopicPartition partition =
          new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
      partitionsList.add(partition);
    }
    return partitionsList;
  }

  private void setPartitionOffsets(List<TopicPartition> allPartitions, long limit) {
    Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(allPartitions);
    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(allPartitions);

    for (TopicPartition partition : allPartitions) {
      if (consumer.position(partition) == 0) {
        // ignore empty partition, otherwise calling consumer.poll() will block thread
        continue;
      }
      consumer.seek(
          partition,
          getUsableOffset(beginningOffsets.get(partition), endOffsets.get(partition), limit));
    }
  }

  private long getUsableOffset(long beginningOffset, long endOffset, long partitionMessageAmount) {
    if ((endOffset - beginningOffset) == 0) {
      return 0;
    }
    if ((endOffset - beginningOffset) >= partitionMessageAmount) {
      return endOffset - partitionMessageAmount;
    }
    return beginningOffset;
  }

  private StreamMessage recordToStreamMessage(ConsumerRecord<Object, Object> item) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("partition", item.partition());
    metadata.put("offset", item.offset());
    metadata.put("timestamp", item.timestamp());
    return new StreamMessage(item.key(), item.value(), metadata);
  }

  public CompletableFuture<Void> deleteStream() {
    return admin.deleteTopics(List.of(this.name)).all().toCompletionStage().toCompletableFuture();
  }

  public void close() {
    Thread newThread =
        new Thread(
            () -> {
              admin.close();
              consumer.close();
            });
    newThread.start();
  }

  private boolean streamExists() {
    try {
      return admin.listTopics().names().get(KAFKA_API_TIMEOUT_MS, TimeUnit.MILLISECONDS).stream()
          .anyMatch(topicName -> topicName.equalsIgnoreCase(this.name));
    } catch (ExecutionException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    } catch (TimeoutException e) {
      close();
      throw new KafkaConnectionException(
          "Connection to node could not be established. Broker may not be available.");
    }
  }

  private static String getDefaultStreamName() {
    return DEFAULT_TOPIC_NAME.orElse("datacater-" + UUID.randomUUID());
  }

  private Map<ConfigResource, Collection<AlterConfigOp>> convertConfigs(
      Map<String, String> configs) {
    Map<ConfigResource, Collection<AlterConfigOp>> topicConfigs = new HashMap<>();
    ConfigResource configResource = new ConfigResource(TOPIC, this.name);
    for (Map.Entry<String, String> entry : configs.entrySet()) {
      ConfigEntry config = new ConfigEntry(entry.getKey(), entry.getValue());
      AlterConfigOp op = new AlterConfigOp(config, AlterConfigOp.OpType.SET);
      topicConfigs.put(configResource, List.of(op));
    }
    return topicConfigs;
  }

  public StreamSpec updateStream(StreamSpec spec) {
    if (streamExists() && Boolean.TRUE.equals(isValidConfig(spec.getConfig()))) {
      KafkaFuture<Void> alterTopicConfigFuture =
          admin.incrementalAlterConfigs(convertConfigs(spec.getConfig())).all();
      try {
        alterTopicConfigFuture.get(KAFKA_API_TIMEOUT_MS.longValue(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        LOGGER.error("Thread got interrupted: ", e);
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) {
        throw new KafkaConnectionException(
            String.format("%s: %s", e.getClass().toString(), e.getMessage()));
      } catch (TimeoutException e) {
        throw new KafkaConnectionException(
            String.format(
                "Exceeded timeout of %d ms when updating the config of the Apache Kafka topic.",
                KAFKA_API_TIMEOUT_MS));
      }
    }
    return spec;
  }

  public StreamSpec createStream(StreamSpec spec) {
    if (!streamExists()) {
      NewTopic newTopic = new NewTopic(this.name, this.partitions, this.replication);
      newTopic.configs(spec.getConfig());
      KafkaFuture<Void> createTopicConfigFuture = admin.createTopics(List.of(newTopic)).all();
      try {
        createTopicConfigFuture.get(KAFKA_API_TIMEOUT_MS.longValue(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        LOGGER.error("Thread got interrupted: ", e);
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) {
        throw new KafkaConnectionException(
            String.format("%s: %s", e.getClass().toString(), e.getMessage()));
      } catch (TimeoutException e) {
        throw new KafkaConnectionException(
            String.format(
                "Exceeded timeout of %d ms when creating Apache Kafka topic.",
                KAFKA_API_TIMEOUT_MS));
      }
      return spec;
    }
    Map<String, String> actualMetaData = getMetadata();
    Map<String, String> configDifferences = compareConfigs(spec.getConfig(), actualMetaData);
    if (configDifferences.containsValue("false")) {
      throw new DatacaterException(
          getCreateExistingStreamErrorMessage(actualMetaData, configDifferences));
    }
    return spec;
  }

  private String getCreateExistingStreamErrorMessage(
      Map<String, String> actualMetaData, Map<String, String> configDifferences) {
    StringBuilder inconsistentConfigBuilder = new StringBuilder();
    for (Map.Entry<String, String> item : configDifferences.entrySet()) {
      if (item.getValue().equals("false")) {
        inconsistentConfigBuilder
            .append(item.getKey())
            .append(": ")
            .append(actualMetaData.get(item.getKey()))
            .append("; ");
      }
    }
    return String.format(
        "Could not create stream as it already exists on the Cluster and the given metadata doesn't match the actual metadata: %s",
        inconsistentConfigBuilder);
  }

  private Map<String, String> compareConfigs(
      Map<String, String> expected, Map<String, String> actual) {
    return expected.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e ->
                    String.valueOf(
                        e.getValue()
                            .equals(actual.getOrDefault(e.getKey(), expected.get(e.getKey()))))));
  }

  private Map<String, String> kafkaConfigToMap(Config conf) {
    Map<String, String> configs = new HashMap<>();
    for (ConfigEntry entry : conf.entries()) {
      configs.put(entry.name(), entry.value());
    }
    return configs;
  }
}

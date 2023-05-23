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
  private final Admin admin;
  private final KafkaConsumer<Object, Object> consumer;

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
        .putIfAbsent(
            StaticConfig.VALUE_DESERIALIZER_TEXT, io.datacater.core.serde.JsonDeserializer.class);
    stream
        .spec()
        .getKafka()
        .putIfAbsent(
            StaticConfig.KEY_DESERIALIZER_TEXT, io.datacater.core.serde.JsonDeserializer.class);
    Admin admin = Admin.create(stream.spec().getKafka());
    KafkaConsumer<Object, Object> consumer = new KafkaConsumer<>(stream.spec().getKafka());

    Optional<String> name = Optional.of(stream.name());

    Integer partitions = getPartitions(stream.spec().getTopic());
    Short replicationFactor = getReplicationFactor(stream.spec().getTopic());

    return new KafkaStreamsAdmin(
        name.orElse(getDefaultStreamName()), partitions, replicationFactor, admin, consumer);
  }

  private static int getPartitions(Map<String, Object> topicConfig) {
    if (topicConfig.get(StaticConfig.PARTITION_COUNT) instanceof String partitions
        && !topicConfig.get(StaticConfig.PARTITION_COUNT).toString().isEmpty()) {
      return Integer.parseInt(partitions);
    }
    return StaticConfig.EnvironmentVariables.DEFAULT_NUM_PARTITIONS;
  }

  private static Short getReplicationFactor(Map<String, Object> topicConfig) {
    if (topicConfig.get(StaticConfig.REPLICATION_FACTOR) instanceof String repFactor
        && !topicConfig.get(StaticConfig.REPLICATION_FACTOR).toString().isEmpty()) {
      return Short.valueOf(repFactor);
    }
    return StaticConfig.EnvironmentVariables.DEFAULT_REPLICATION_FACTOR;
  }

  void createDefaultChannel() {
    NewTopic newTopic =
        new NewTopic(
            getDefaultStreamName(),
            StaticConfig.EnvironmentVariables.DEFAULT_NUM_PARTITIONS,
            StaticConfig.EnvironmentVariables.DEFAULT_REPLICATION_FACTOR);
    admin.createTopics(List.of(newTopic));
  }

  public Boolean isValidConfig(Map<String, String> config) {
    Optional<String> newPartitions = Optional.ofNullable(config.get(StaticConfig.PARTITION_COUNT));
    Optional<String> newReplicationFactor =
        Optional.ofNullable(config.get(StaticConfig.REPLICATION_FACTOR));
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
                throw new DatacaterException(
                    StaticConfig.LoggerMessages.KAFKA_TOPIC_METADATA_NOT_MAPPED);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DatacaterException(
                    StaticConfig.LoggerMessages.KAFKA_TOPIC_METADATA_NOT_MAPPED_INTERRUPTED);
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
        consumer.poll(Duration.ofMillis(StaticConfig.EnvironmentVariables.KAFKA_API_TIMEOUT_MS));

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
    metadata.put(StaticConfig.PARTITION_TEXT, item.partition());
    metadata.put(StaticConfig.OFFSET_TEXT, item.offset());
    metadata.put(StaticConfig.TIMESTAMP_TEXT, item.timestamp());
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
      return admin
          .listTopics()
          .names()
          .get(StaticConfig.EnvironmentVariables.KAFKA_API_TIMEOUT_MS, TimeUnit.MILLISECONDS)
          .stream()
          .anyMatch(topicName -> topicName.equalsIgnoreCase(this.name));
    } catch (ExecutionException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    } catch (TimeoutException e) {
      close();
      throw new KafkaConnectionException(StaticConfig.LoggerMessages.KAFKA_BROKER_UNAVAILABLE);
    }
  }

  private static String getDefaultStreamName() {
    return StaticConfig.EnvironmentVariables.DEFAULT_TOPIC_NAME.orElse(
        StaticConfig.DATACATER_PREFIX + UUID.randomUUID());
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
        alterTopicConfigFuture.get(
            StaticConfig.EnvironmentVariables.KAFKA_API_TIMEOUT_MS.longValue(),
            TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        LOGGER.error(StaticConfig.LoggerMessages.THREAD_INTERRUPTED, e);
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) {
        throw new KafkaConnectionException(
            String.format("%s: %s", e.getClass().toString(), e.getMessage()));
      } catch (TimeoutException e) {
        throw new KafkaConnectionException(
            String.format(
                StaticConfig.LoggerMessages.TOPIC_UPDATE_TIMEOUT_FORMATTED,
                StaticConfig.EnvironmentVariables.KAFKA_API_TIMEOUT_MS));
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
        createTopicConfigFuture.get(
            StaticConfig.EnvironmentVariables.KAFKA_API_TIMEOUT_MS.longValue(),
            TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        LOGGER.error(StaticConfig.LoggerMessages.THREAD_INTERRUPTED, e);
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) {
        throw new KafkaConnectionException(
            String.format("%s: %s", e.getClass().toString(), e.getMessage()));
      } catch (TimeoutException e) {
        throw new KafkaConnectionException(
            String.format(
                StaticConfig.LoggerMessages.TOPIC_UPDATE_TIMEOUT_FORMATTED,
                StaticConfig.EnvironmentVariables.KAFKA_API_TIMEOUT_MS));
      }
      return spec;
    }
    Map<String, String> actualMetaData = getMetadata();
    Map<String, String> configDifferences = compareConfigs(spec.getConfig(), actualMetaData);
    if (configDifferences.containsValue(StaticConfig.FALSE)) {
      throw new DatacaterException(
          getCreateExistingStreamErrorMessage(actualMetaData, configDifferences));
    }
    return spec;
  }

  private String getCreateExistingStreamErrorMessage(
      Map<String, String> actualMetaData, Map<String, String> configDifferences) {
    StringBuilder inconsistentConfigBuilder = new StringBuilder();
    for (Map.Entry<String, String> item : configDifferences.entrySet()) {
      if (item.getValue().equals(StaticConfig.FALSE)) {
        inconsistentConfigBuilder
            .append(item.getKey())
            .append(": ")
            .append(actualMetaData.get(item.getKey()))
            .append("; ");
      }
    }
    return String.format(
        StaticConfig.LoggerMessages.STREAM_NOT_CREATED_METADATA_MISMATCH_FORMATTED,
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

package io.datacater.core.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.exceptions.CreateDeploymentException;
import io.datacater.core.exceptions.DatacaterException;
import io.datacater.core.exceptions.DeleteStreamException;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.utilities.LoggerUtilities;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StreamUtilities {
  private static final Logger LOGGER = Logger.getLogger(StreamUtilities.class);
  @Inject DataCaterSessionFactory dsf;

  public Uni<List<StreamMessage>> getStreamMessages(UUID uuid) {
    return getStreamMessages(
        uuid, StaticConfig.STREAM_AMOUNT_MESSAGE_LIMIT, SampleMethod.SEQUENCED);
  }

  public void updateStreamObject(Stream stream, List<ConfigEntity> configList)
      throws JsonProcessingException {
    Stream streamWithConfig = ConfigUtilities.applyConfigsToStream(Stream.from(stream), configList);
    StreamService kafkaAdmin = KafkaStreamsAdmin.from(streamWithConfig);
    kafkaAdmin.updateStream(streamWithConfig.spec());
    kafkaAdmin.close();
  }

  public void deleteStreamObject(Stream stream, List<ConfigEntity> configList) {
    stream = ConfigUtilities.applyConfigsToStream(stream, configList);
    StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
    try {
      kafkaAdmin
          .deleteStream()
          .get(StaticConfig.EnvironmentVariables.KAFKA_API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      throw new DeleteStreamException(e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DeleteStreamException(e.getMessage());
    } catch (TimeoutException e) {
      LoggerUtilities.logExceptionMessage(
          LOGGER, new Throwable().getStackTrace()[0].getMethodName(), e.getMessage());
      LOGGER.info(StaticConfig.LoggerMessages.STREAM_DELETE_NOT_FINISHED_MESSAGE);
    }
  }

  public void createStreamObject(Stream stream, List<ConfigEntity> configList) {
    stream = ConfigUtilities.applyConfigsToStream(stream, configList);
    StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
    kafkaAdmin.createStream(stream.spec());
    kafkaAdmin.close();
  }

  public Uni<List<StreamMessage>> getStreamMessages(
      UUID uuid, Long limit, SampleMethod sampleMethod) {
    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .find(StreamEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .transformToUni(
                    entity -> {
                      try {
                        Stream stream = Stream.from(entity);
                        Uni<List<ConfigEntity>> configList =
                            ConfigUtilities.getMappedConfigs(stream.configSelector(), session);
                        return Uni.combine()
                            .all()
                            .unis(Uni.createFrom().item(stream), configList)
                            .asTuple();
                      } catch (JsonProcessingException ex) {
                        throw new DatacaterException(ex.getMessage());
                      }
                    })
                .onItem()
                .ifNotNull()
                .transform(
                    Unchecked.function(
                        tuple -> {
                          Stream stream = tuple.getItem1();
                          stream = ConfigUtilities.applyConfigsToStream(stream, tuple.getItem2());
                          // Overwrite Kafka Consumer property `max.poll.records` with the
                          // parameter `limit`
                          stream
                              .spec()
                              .getKafka()
                              .put(StaticConfig.MAX_POLL_RECORDS, limit.intValue());
                          StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
                          List<StreamMessage> messages =
                              kafkaAdmin.inspect(stream, limit, sampleMethod);
                          kafkaAdmin.close();
                          return messages;
                        }))));
  }

  public Uni<Stream> getStreamFromDeployment(
      DeploymentSpec spec, String deploymentSpecKey, PipelineEntity pipeline, String key) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(
                    StreamEntity.class,
                    getStreamUUIDFromDeployment(
                        spec, deploymentSpecKey, pipeline.getMetadata(), key))
                .onItem()
                .ifNotNull()
                .transformToUni(
                    entity -> {
                      try {
                        Stream stream = Stream.from(entity);
                        Uni<List<ConfigEntity>> configList =
                            ConfigUtilities.getMappedConfigs(stream.configSelector(), session);
                        return Uni.combine()
                            .all()
                            .unis(Uni.createFrom().item(stream), configList)
                            .asTuple();
                      } catch (JsonProcessingException ex) {
                        LoggerUtilities.logExceptionMessage(
                            LOGGER,
                            new Throwable().getStackTrace()[0].getMethodName(),
                            ex.getMessage());
                        throw new DatacaterException(ex.getMessage());
                      }
                    })
                .onItem()
                .ifNotNull()
                .transform(
                    tuple -> {
                      Stream stream = tuple.getItem1();
                      stream = ConfigUtilities.applyConfigsToStream(stream, tuple.getItem2());
                      return stream;
                    })
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(
                        String.format(StaticConfig.LoggerMessages.STREAM_NOT_FOUND, key))));
  }

  public UUID getStreamUUIDFromDeployment(
      DeploymentSpec spec, String deploymentSpecKey, JsonNode node, String key) {
    // try and get from deploymentSpec
    String uuidString = "";
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> streamConfig =
        mapper.convertValue(spec.deployment().get(deploymentSpecKey), Map.class);
    if (streamConfig != null && streamConfig.get(StaticConfig.UUID_TEXT) != null) {
      uuidString = streamConfig.get(StaticConfig.UUID_TEXT).toString();
    }

    // if not in deploymentSpec, try and get from pipeline
    if ((uuidString == null || uuidString.isEmpty() || uuidString.isBlank())
        && node.get(key) != null) {
      uuidString = node.get(key).asText();
    }

    // if in neither, throw exception
    if (uuidString == null || uuidString.isEmpty() || uuidString.isBlank()) {
      throw new CreateDeploymentException(
          String.format(StaticConfig.LoggerMessages.STREAM_NOT_FOUND, key));
    }

    return UUID.fromString(uuidString);
  }
}

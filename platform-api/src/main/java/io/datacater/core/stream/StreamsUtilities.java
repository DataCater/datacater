package io.datacater.core.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.DatacaterException;
import io.datacater.core.exceptions.DeleteStreamException;
import io.datacater.core.utilities.LoggerUtilities;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StreamsUtilities {
  private static final Logger LOGGER = Logger.getLogger(StreamsUtilities.class);

  public static final Integer KAFKA_API_TIMEOUT_MS =
      ConfigProvider.getConfig()
          .getOptionalValue("kafka.api.timeout.ms", Integer.class)
          .orElse(5000);

  public static final String streamNotFoundMessage = "Stream not found.";
  public static final String streamDeleteNotFinishedMessage =
      "Stream deletion was called without errors but has not finished yet.";
  @Inject DataCaterSessionFactory dsf;

  public Uni<List<StreamMessage>> getStreamMessages(UUID uuid) {
    return getStreamMessages(uuid, 100L, SampleMethod.SEQUENCED);
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
      kafkaAdmin.deleteStream().get(KAFKA_API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      throw new DeleteStreamException(e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DeleteStreamException(e.getMessage());
    } catch (TimeoutException e) {
      LoggerUtilities.logExceptionMessage(
          LOGGER, new Throwable().getStackTrace()[0].getMethodName(), e.getMessage());
      LOGGER.info(streamDeleteNotFinishedMessage);
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
                          stream.spec().getKafka().put("max.poll.records", limit.intValue());
                          StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
                          List<StreamMessage> messages =
                              kafkaAdmin.inspect(stream, limit, sampleMethod);
                          kafkaAdmin.close();
                          return messages;
                        }))));
  }
}

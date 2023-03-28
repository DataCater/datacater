package io.datacater.core.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.DatacaterException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class StreamsUtilities {

  @Inject DataCaterSessionFactory dsf;

  public Uni<List<StreamMessage>> getStreamMessages(UUID uuid) {
    return getStreamMessages(uuid, 100L, SampleMethod.SEQUENCED);
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

package io.datacater.core.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datacater.core.authentication.DataCaterSessionFactory;
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
    return getStreamMessages(uuid, 100L, false);
  }

  public Uni<List<StreamMessage>> getStreamMessages(
      UUID uuid, Long limit, boolean distributedInspect) {
    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .find(StreamEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .transform(
                    Unchecked.function(
                        e -> {
                          try {
                            Stream stream = Stream.from(e);
                            // Overwrite Kafka Consumer property `max.poll.records` with the
                            // parameter `limit`
                            stream.spec().getKafka().put("max.poll.records", limit.intValue());
                            StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
                            List<StreamMessage> messages =
                                kafkaAdmin.inspect(stream, limit, distributedInspect);
                            kafkaAdmin.close();
                            return messages;
                          } catch (JsonProcessingException ex) {
                            throw new DatacaterException(ex.getMessage());
                          }
                        }))));
  }
}

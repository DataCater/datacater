package io.datacater.core.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datacater.core.exceptions.DatacaterException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class StreamsUtilities {

  @Inject Mutiny.SessionFactory sf;

  public Uni<List<StreamMessage>> getStreamMessages(UUID uuid) {
    return getStreamMessages(uuid, 100);
  }

  public Uni<List<StreamMessage>> getStreamMessages(UUID uuid, long limit) {
    return sf.withTransaction(
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
                            StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
                            List<StreamMessage> messages = kafkaAdmin.inspect(stream, limit);
                            kafkaAdmin.close();
                            return messages;
                          } catch (JsonProcessingException ex) {
                            throw new DatacaterException(ex.getMessage());
                          }
                        }))));
  }
}

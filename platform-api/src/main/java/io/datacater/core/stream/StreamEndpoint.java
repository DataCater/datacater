package io.datacater.core.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.exceptions.*;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.logging.Logger;

@Path("/streams")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "apiToken")
public class StreamEndpoint {
  private static final Logger LOGGER = Logger.getLogger(StreamEndpoint.class);
  private static final Integer KAFKA_API_TIMEOUT_MS =
      ConfigProvider.getConfig()
          .getOptionalValue("kafka.api.timeout.ms", Integer.class)
          .orElse(5000);
  @Inject DataCaterSessionFactory dsf;
  @Inject StreamsUtilities streamsUtil;

  @GET
  @Path("{uuid}")
  public Uni<StreamEntity> getStream(@PathParam("uuid") UUID uuid) {
    return dsf.withTransaction(((session, transaction) -> session.find(StreamEntity.class, uuid)))
        .onItem()
        .ifNull()
        .failWith(new StreamNotFoundException("Stream not found."));
  }

  @GET
  @Path("{uuid}/inspect")
  public Uni<List<StreamMessage>> inspectStream(
      @PathParam("uuid") UUID uuid,
      @DefaultValue("100") @QueryParam("limit") Long limit,
      @DefaultValue("false") @QueryParam("distributed") boolean distributed) {
    return streamsUtil.getStreamMessages(uuid, limit, distributed);
  }

  @GET
  public Uni<List<StreamEntity>> getStreams() {
    return dsf.withSession(
        session -> session.createQuery("from StreamEntity", StreamEntity.class).getResultList());
  }

  @POST
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> createStream(Stream stream) throws JsonProcessingException {
    StreamEntity se = new StreamEntity(stream.name(), stream.spec());

    return dsf.withTransaction(
            (session, transaction) ->
                session
                    .persist(se)
                    .onItem()
                    .transform(
                        a -> {
                          createStreamObject(stream);
                          return a;
                        })
                    .replaceWith(Response.ok(se).build()))
        .onFailure()
        .transform(
            ex -> new CreateStreamException(exceptionCauseMessageIfAvailable((Exception) ex)));
  }

  @PUT
  @Path("{uuid}")
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<StreamEntity> updateStream(@PathParam("uuid") UUID uuid, Stream stream) {
    return dsf.withTransaction(
            ((session, transaction) ->
                session
                    .find(StreamEntity.class, uuid)
                    .call(
                        Unchecked.function(
                            se -> {
                              LOGGER.info(se);
                              updateStreamObject(stream);
                              return session.merge((se).updateEntity(stream));
                            }))))
        .onFailure()
        .transform(
            ex -> new UpdateStreamException(exceptionCauseMessageIfAvailable((Exception) ex)));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteStream(
      @PathParam("uuid") UUID uuid, @HeaderParam("force") Boolean force) {
    return dsf.withTransaction(
        ((session, tx) ->
            session
                .find(StreamEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .call(
                    se -> {
                      if (Boolean.TRUE.equals(force)) {
                        try {
                          deleteStreamObject(Stream.from(se));
                        } catch (JsonProcessingException e) {
                          throw new DatacaterException(
                              "Could not convert StreamEntity to StreamObject: " + e.getMessage());
                        }
                      }
                      return session.remove(se);
                    })
                .replaceWith(Response.ok().build())));
  }

  private void updateStreamObject(Stream stream) {
    StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
    kafkaAdmin.updateStream(stream.spec());
    kafkaAdmin.close();
  }

  private void deleteStreamObject(Stream stream) {
    StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
    try {
      kafkaAdmin.deleteStream().get(KAFKA_API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      throw new DatacaterException(e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DatacaterException(e.getMessage());
    } catch (TimeoutException e) {
      LOGGER.info("Stream deletion was called without errors but has not finished yet.");
    }
  }

  private void createStreamObject(Stream stream) {
    StreamService kafkaAdmin = KafkaStreamsAdmin.from(stream);
    kafkaAdmin.createStream(stream.spec());
    kafkaAdmin.close();
  }

  private static String exceptionCauseMessageIfAvailable(Exception ex) {
    if (ex.getCause() == null) {
      return ex.getMessage();
    }
    return ex.getCause().getMessage();
  }
}

package io.datacater.core.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.*;
import io.datacater.core.project.ProjectUtilities;
import io.datacater.core.utilities.LoggerUtilities;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.logging.Logger;

@Path("{project}/streams")
@Authenticated
@SecurityRequirement(name = "apiToken")
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
public class StreamEndpoint {
  private static final Logger LOGGER = Logger.getLogger(StreamEndpoint.class);
  @Inject DataCaterSessionFactory dsf;
  @Inject StreamUtilities streamUtil;
  @Inject ProjectUtilities projectUtil;

  @GET
  @Path("{uuid}")
  public Uni<StreamEntity> getStream(
      @PathParam("project") String project, @PathParam("uuid") UUID uuid) {
    return dsf.withTransaction(((session, transaction) -> session.find(StreamEntity.class, uuid)))
        .onItem()
        .ifNull()
        .failWith(new StreamNotFoundException(StaticConfig.LoggerMessages.STREAM_NOT_FOUND_MESSAGE))
        .onItem()
        .ifNotNull()
        .transform(
            item -> {
              if (item.getProject().equals(project)) {
                return item;
              }
              return null;
            });
  }

  @GET
  @Path("{uuid}/inspect")
  public Uni<List<StreamMessage>> inspectStream(
      @PathParam("project") String project,
      @PathParam("uuid") UUID uuid,
      @DefaultValue("100") @QueryParam("limit") Long limit,
      @DefaultValue("SEQUENCED") @QueryParam("sampleMethod") SampleMethod sampleMethod) {
    return streamUtil.getStreamMessages(uuid, limit, sampleMethod, project);
  }

  @GET
  public Uni<List<StreamEntity>> getStreams(@PathParam("project") String project) {
    return dsf.withSession(
        session ->
            session
                .createQuery("from StreamEntity", StreamEntity.class)
                .getResultList()
                .onItem()
                .ifNull()
                .continueWith(List.of())
                .onItem()
                .ifNotNull()
                .transform(
                    list ->
                        list.stream().filter(item -> item.getProject().equals(project)).toList())
                .onFailure()
                .recoverWithItem(List.of()));
  }

  @POST
  @RequestBody
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<Response> createStream(@PathParam("project") String project, Stream stream)
      throws JsonProcessingException {
    StreamEntity se =
        new StreamEntity(stream.name(), stream.spec(), stream.configSelector(), project);
    Uni<Response> persistResponse =
        dsf.withTransaction(
                (session, transaction) ->
                    session
                        .persist(se)
                        .onItem()
                        .transformToUni(
                            voidObject ->
                                ConfigUtilities.getMappedConfigs(stream.configSelector(), session))
                        .onItem()
                        .transform(
                            configEntities -> {
                              streamUtil.createStreamObject(stream, configEntities);
                              return configEntities;
                            })
                        .replaceWith(Response.ok(se).build()))
            .onFailure()
            .transform(
                ex -> {
                  LoggerUtilities.logExceptionMessage(
                      LOGGER, new Throwable().getStackTrace()[0].getMethodName(), ex.getMessage());
                  return new CreateStreamException(
                      LoggerUtilities.getExceptionCauseIfAvailable((Exception) ex));
                });

    return projectUtil.findProjectAndPersist(project, persistResponse);
  }

  @PUT
  @Path("{uuid}")
  @RequestBody
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<StreamEntity> updateStream(
      @PathParam("project") String project, @PathParam("uuid") UUID uuid, Stream stream) {

    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .find(StreamEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .transform(
                    item -> {
                      if (item.getProject().equals(project)) {
                        return item;
                      }
                      return null;
                    })
                .onItem()
                .transformToUni(
                    streamEntity ->
                        Uni.combine()
                            .all()
                            .unis(
                                Uni.createFrom().item(streamEntity),
                                ConfigUtilities.getMappedConfigs(stream.configSelector(), session))
                            .asTuple())
                .onItem()
                .transform(
                    tuple -> {
                      try {
                        streamUtil.updateStreamObject(stream, tuple.getItem2());
                        return session.merge((tuple.getItem1()).updateEntity(stream));
                      } catch (JsonProcessingException e) {
                        LoggerUtilities.logExceptionMessage(
                            LOGGER,
                            new Throwable().getStackTrace()[0].getMethodName(),
                            e.getMessage());
                        // A generic Exception is thrown to be caught further down.
                        throw new RuntimeException(e);
                      }
                    })
                .flatMap(streamEntity -> streamEntity)
                .onFailure()
                .transform(
                    ex ->
                        new UpdateStreamException(
                            LoggerUtilities.getExceptionCauseIfAvailable((Exception) ex)))));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteStream(
      @PathParam("project") String project,
      @PathParam("uuid") UUID uuid,
      @HeaderParam("force") Boolean force) {
    return dsf.withTransaction(
        ((session, tx) ->
            session
                .find(StreamEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .transform(
                    item -> {
                      if (item.getProject().equals(project)) {
                        return item;
                      }
                      return null;
                    })
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
                            .unis(
                                Uni.createFrom().item(stream),
                                configList,
                                Uni.createFrom().item(entity))
                            .asTuple();
                      } catch (JsonProcessingException ex) {
                        LoggerUtilities.logExceptionMessage(
                            LOGGER,
                            new Throwable().getStackTrace()[0].getMethodName(),
                            ex.getMessage());
                        throw new DeleteStreamException(ex.getMessage());
                      }
                    })
                .onItem()
                .ifNotNull()
                .call(
                    tuple -> {
                      if (Boolean.TRUE.equals(force)) {
                        streamUtil.deleteStreamObject(tuple.getItem1(), tuple.getItem2());
                      }
                      return session.remove(tuple.getItem3());
                    })
                .replaceWith(Response.ok().build())));
  }
}

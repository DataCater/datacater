package io.datacater.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datacater.core.exceptions.ConfigNotFoundException;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;

@Path("/configs")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigEndpoint {
  @Inject SessionFactory sf;

  @GET
  public Uni<List<ConfigEntity>> getAllConfigs() {
    return sf.withTransaction(
        session ->
            session
                .createQuery("from ConfigEntity", ConfigEntity.class)
                .getResultList()
                .onItem()
                .ifNull()
                .continueWith(List.of()));
  }

  @GET
  @Path("{uuid}")
  public Uni<ConfigEntity> getConfig(@PathParam("uuid") UUID uuid) {
    return sf.withTransaction(
        session ->
            session
                .find(ConfigEntity.class, uuid)
                .onItem()
                .ifNull()
                .failWith(
                    new ConfigNotFoundException(
                        String.format("No config found for uuid %s", uuid.toString()))));
  }

  @POST
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<ConfigEntity> createConfig(Config config) throws JsonProcessingException {
    ConfigEntity configEntity = ConfigEntity.from(config.name(), config.kind(), config.config());

    return sf.withTransaction((session, transaction) -> session.persist(configEntity))
        .replaceWith(configEntity);
  }

  @PUT
  @Path("{uuid}")
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<ConfigEntity> updateConfig(@PathParam("uuid") UUID uuid, Config config) {
    return sf.withTransaction(
        ((session, transaction) ->
            session
                .find(ConfigEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .call(configEntity -> session.merge(configEntity.updateEntity(config)))
                .onItem()
                .ifNull()
                .failWith(
                    new ConfigNotFoundException(
                        String.format("No config found for uuid %s", uuid.toString())))));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteConfig(@PathParam("uuid") UUID uuid) {
    return sf.withTransaction(
        ((session, transaction) ->
            session
                .find(ConfigEntity.class, uuid)
                .onItem()
                .ifNull()
                .failWith(
                    new ConfigNotFoundException(
                        String.format("No config found for uuid %s", uuid.toString())))
                .onItem()
                .ifNotNull()
                .call(session::remove)
                .replaceWith(Response.ok().build())));
  }
}

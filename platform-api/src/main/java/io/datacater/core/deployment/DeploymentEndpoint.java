package io.datacater.core.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@Path("/api/alpha/deployments")
@RolesAllowed("dev")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class DeploymentEndpoint {
  private static final Logger LOGGER = Logger.getLogger(DeploymentEndpoint.class);
  @Inject Mutiny.SessionFactory sf;

  @Inject KubernetesClient client;

  @GET
  @Path("{uuid}")
  public Uni<DeploymentEntity> getStream(@PathParam("uuid") UUID uuid) {
    return sf.withTransaction(
        ((session, transaction) -> session.find(DeploymentEntity.class, uuid)));
  }

  @GET
  @Path("{uuid}/logs")
  // TODO really return string and plaintext? might be a better way, look it up
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> getLogs(@PathParam("uuid") UUID uuid) {
    return sf.withTransaction(
        ((session, transaction) ->
            session
                .find(DeploymentEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .transform(this::getLogs)));
  }

  @GET
  public Uni<List<DeploymentEntity>> getDeployments() {
    return sf.withSession(
        session ->
            session.createQuery("from DeploymentEntity", DeploymentEntity.class).getResultList());
  }

  @POST
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> createDeployment(DatacaterDeployment datacaterDeployment)
      throws JsonProcessingException {
    DeploymentEntity de = new DeploymentEntity(datacaterDeployment.spec());
    return sf.withTransaction(
            (session, transaction) ->
                session
                    .find(PipelineEntity.class, datacaterDeployment.spec().getPipelineId())
                    .onItem()
                    .ifNotNull()
                    .call(
                        pe ->
                            session
                                .find(
                                    StreamEntity.class, getUUIDFromNode(pe, StaticConfig.STREAM_IN))
                                .onItem()
                                .ifNotNull()
                                .call(
                                    streamIn ->
                                        session
                                            .find(
                                                StreamEntity.class,
                                                getUUIDFromNode(pe, StaticConfig.STREAM_OUT))
                                            .onItem()
                                            .ifNotNull()
                                            .call(
                                                streamOut ->
                                                    session.persist(
                                                        createDeployment(
                                                            datacaterDeployment,
                                                            pe,
                                                            streamOut,
                                                            streamIn,
                                                            de))))))
        .replaceWith(Response.ok(de).build());
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteDeployment(@PathParam("uuid") UUID uuid) {
    return sf.withTransaction(
        ((session, tx) ->
            session
                .find(DeploymentEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .call(de -> session.remove(deleteDeployment(de)))
                .replaceWith(Response.ok().build())));
  }

  @PUT
  @Path("{uuid}")
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<DeploymentEntity> updateDeployment(
      @PathParam("uuid") UUID uuid, DatacaterDeployment datacaterDeployment) {
    return sf.withTransaction(
        (session, transaction) ->
            session
                .find(DeploymentEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .call(
                    de ->
                        session
                            .find(PipelineEntity.class, datacaterDeployment.spec().getPipelineId())
                            .onItem()
                            .ifNull()
                            .continueWith(new PipelineEntity())
                            .call(
                                pe ->
                                    session
                                        .find(
                                            StreamEntity.class,
                                            getUUIDFromNode(pe, StaticConfig.STREAM_IN))
                                        .onItem()
                                        .ifNotNull()
                                        .call(
                                            streamIn ->
                                                session
                                                    .find(
                                                        StreamEntity.class,
                                                        getUUIDFromNode(
                                                            pe, StaticConfig.STREAM_OUT))
                                                    .onItem()
                                                    .ifNotNull()
                                                    .call(
                                                        streamOut ->
                                                            session.persist(
                                                                createDeployment(
                                                                    datacaterDeployment,
                                                                    pe,
                                                                    streamOut,
                                                                    streamIn,
                                                                    de)))))));
  }

  private String getLogs(DeploymentEntity de) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getLogs(de.getName());
  }

  private DeploymentEntity deleteDeployment(DeploymentEntity de) {
    K8Deployment k8Deployment = new K8Deployment(client);
    k8Deployment.delete(de.getName());
    return de;
  }

  private DeploymentEntity createDeployment(
      DatacaterDeployment datacaterDeployment,
      PipelineEntity pe,
      StreamEntity streamOut,
      StreamEntity streamIn,
      DeploymentEntity de) {
    K8Deployment k8Deployment = new K8Deployment(client);
    String name = k8Deployment.create(datacaterDeployment, pe, streamIn, streamOut);
    de.setName(name);
    de.setSpec(datacaterDeployment.spec());
    return de;
  }

  private UUID getUUIDFromNode(PipelineEntity pe, String node) {
    return UUID.fromString(pe.getMetadata().get(node).asText());
  }
}

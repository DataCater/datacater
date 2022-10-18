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
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> getLogs(@PathParam("uuid") UUID uuid) {
    return sf.withTransaction(
        ((session, transaction) ->
            session
                .find(DeploymentEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .transform(
                    de -> {
                      KubernetesDeployment k8Deploy = new KubernetesDeployment(client);
                      return k8Deploy.getDeploymentLogs(de.getName());
                    })));
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
                        x ->
                            session
                                .find(
                                    StreamEntity.class,
                                    UUID.fromString(x.getMetadata().get("stream-in").asText()))
                                .onItem()
                                .ifNotNull()
                                .call(
                                    streamin ->
                                        session
                                            .find(
                                                StreamEntity.class,
                                                UUID.fromString(
                                                    x.getMetadata().get("stream-out").asText()))
                                            .onItem()
                                            .ifNotNull()
                                            .call(
                                                streamout -> {
                                                  KubernetesDeployment k8Deploy =
                                                      new KubernetesDeployment(client);
                                                  String name =
                                                      k8Deploy.createDeployment(
                                                          datacaterDeployment,
                                                          x,
                                                          streamin,
                                                          streamout);
                                                  de.setName(name);
                                                  return session.persist(de);
                                                }))))
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
                .call(
                    de -> {
                      KubernetesDeployment k8Deploy = new KubernetesDeployment(client);
                      k8Deploy.deleteDeployment(de.getName());
                      return session.remove(de);
                    })
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
                                x -> // find serializers and bootstrap.servers from config
                                session
                                        .find(
                                            StreamEntity.class,
                                            UUID.fromString(
                                                x.getMetadata().get("stream-in").asText()))
                                        .onItem()
                                        .ifNotNull()
                                        .call(
                                            streamin ->
                                                session
                                                    .find(
                                                        StreamEntity.class,
                                                        UUID.fromString(
                                                            x.getMetadata()
                                                                .get("stream-out")
                                                                .asText()))
                                                    .onItem()
                                                    .ifNotNull()
                                                    .call(
                                                        streamout -> {
                                                          KubernetesDeployment k8Deploy =
                                                              new KubernetesDeployment(client);
                                                          String name =
                                                              k8Deploy.createDeployment(
                                                                  datacaterDeployment,
                                                                  x,
                                                                  streamin,
                                                                  streamout);
                                                          de.setName(name);
                                                          de.setSpec(datacaterDeployment.spec());
                                                          return session.persist(de);
                                                        })))));
  }
}

package io.datacater.core.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.exceptions.*;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.smallrye.mutiny.Uni;
import java.io.*;
import java.util.List;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@Path("/deployments")
@RolesAllowed("dev")
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "apiToken")
@RequestScoped
public class DeploymentEndpoint {
  private static final Logger LOGGER = Logger.getLogger(DeploymentEndpoint.class);
  @Inject Mutiny.SessionFactory sf;

  @Inject KubernetesClient client;

  @GET
  @Path("{uuid}")
  public Uni<DeploymentEntity> getDeployment(@PathParam("uuid") UUID deploymentId) {
    return sf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .transform(
            deployment -> {
              try {
                return getK8Deployment(deployment);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            })
        .onItem()
        .ifNull()
        .failWith(
            new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND));
  }

  @GET
  @Path("{uuid}/logs")
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> getLogs(@PathParam("uuid") UUID deploymentId) {
    return Uni.createFrom().item(getDeploymentLogs(deploymentId));
  }

  @GET
  @Path("{uuid}/watch-logs")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Response watchLogs(
      @PathParam("uuid") UUID deploymentId, @Context Sse sse, @Context SseEventSink eventSink)
      throws IOException {
    LogWatch lw = watchDeploymentLogs(deploymentId).watchLog();
    InputStream is = lw.getOutput();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      final OutboundSseEvent sseEvent = sse.newEventBuilder().data(line).build();

      eventSink.send(sseEvent);
    }
    is.close();
    lw.close();
    return Response.ok().build();
  }

  @GET
  public Uni<List<DeploymentEntity>> getDeployments() {
    return sf.withSession(
        session ->
            session
                .createQuery("from DeploymentEntity", DeploymentEntity.class)
                .getResultList()
                .onItem()
                .transform(
                    list -> {
                      try {
                        return getK8Deployments(list);
                      } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                      }
                    }));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<DeploymentEntity> createDeployment(io.datacater.core.deployment.Deployment deployment)
      throws JsonProcessingException {
    DeploymentEntity de = new DeploymentEntity(deployment.spec());

    Uni<PipelineEntity> pipelineUni = getPipeline(deployment.spec());
    Uni<StreamEntity> streamInUni = getStream(pipelineUni, StaticConfig.STREAM_IN);
    Uni<StreamEntity> streamOutUni = getStream(pipelineUni, StaticConfig.STREAM_OUT);

    return sf.withSession(
        session ->
            session
                .persist(de)
                .flatMap(
                    empty -> {
                      LOGGER.info("starting chain");
                      return pipelineUni.flatMap(
                          pipelineEntity -> {
                            LOGGER.info("found pipeline in chain: " + pipelineEntity.getId());
                            return streamInUni.flatMap(
                                streamIn -> {
                                  LOGGER.info("found pipeline in chain: " + pipelineEntity.getId());
                                  return streamOutUni.map(
                                      streamOut -> {
                                        LOGGER.info(
                                            "found pipeline in chain: " + pipelineEntity.getId());
                                        return createDeployment(
                                            pipelineEntity,
                                            streamOut,
                                            streamIn,
                                            deployment.spec(),
                                            de);
                                      });
                                });
                          });
                    }));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteDeployment(@PathParam("uuid") UUID deploymentId) {
    deleteK8Deployment(deploymentId);
    return Uni.createFrom().item(Response.ok().build());
  }

  //  @PUT
  //  @Path("{uuid}")
  //  public Uni<DeploymentEntity> updateDeployment(
  //      @PathParam("uuid") UUID deploymentUuid, io.datacater.core.deployment.Deployment
  // deployment) {
  //    return sf.withTransaction(
  //            ((session, transaction) ->
  //                session
  //                    .find(DeploymentEntity.class, deploymentUuid)
  //                    .onItem()
  //                    .ifNotNull()
  //                    .transformToUni(
  //                        Unchecked.function(
  //                            de -> {
  //                              deleteK8Deployment(deploymentUuid);
  //                              apply(session, deployment.spec(), de);
  //                              return session.merge((de));
  //                            }))))
  //        .onFailure()
  //        .transform(
  //            ex ->
  //                new
  // UpdateDeploymentException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_UPDATED));
  //  }

  private Uni<PipelineEntity> getPipeline(DeploymentSpec deploymentSpec) {
    return sf.withSession(
        session ->
            session
                .find(
                    PipelineEntity.class,
                    UUID.fromString(
                        deploymentSpec
                            .deployment()
                            .get(StaticConfig.PIPELINE_NODE_TEXT)
                            .toString()))
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(StaticConfig.LoggerMessages.PIPELINE_NOT_FOUND))
                .onItem()
                .ifNotNull()
                .transform(
                    x -> {
                      LOGGER.info("getPipeline" + x.getId());
                      return x;
                    }));
  }

  private Uni<StreamEntity> getStream(Uni<PipelineEntity> pipelineUni, String key) {
    return sf.withSession(
        session ->
            session
                .find(
                    StreamEntity.class,
                    pipelineUni.map(pe -> getUUIDFromNode(pe.getMetadata(), key)))
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(
                        String.format(StaticConfig.LoggerMessages.STREAM_NOT_FOUND, key)))
                .onItem()
                .ifNotNull()
                .transform(
                    x -> {
                      LOGGER.info("getStream" + x.getId());
                      return x;
                    }));
  }

  private String getDeploymentLogs(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getLogs(deploymentId);
  }

  private RollableScalableResource<Deployment> watchDeploymentLogs(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.watchLogs(deploymentId);
  }

  private List<DeploymentEntity> getK8Deployments(List<DeploymentEntity> deployments)
      throws JsonProcessingException {
    K8Deployment k8Deployment = new K8Deployment(client);
    for (DeploymentEntity deployment : deployments) {
      deployment.setSpec(
          DeploymentSpec.serializeDeploymentSpec(
              k8Deployment.getDeployment(deployment.getId()).deployment()));
    }
    return deployments;
  }

  private DeploymentEntity getK8Deployment(DeploymentEntity deployment)
      throws JsonProcessingException {
    K8Deployment k8Deployment = new K8Deployment(client);
    deployment.setSpec(
        DeploymentSpec.serializeDeploymentSpec(
            k8Deployment.getDeployment(deployment.getId()).deployment()));
    return deployment;
  }

  private void deleteK8Deployment(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    k8Deployment.delete(deploymentId);
  }

  private DeploymentEntity createDeployment(
      PipelineEntity pe,
      StreamEntity streamOut,
      StreamEntity streamIn,
      DeploymentSpec deploymentSpec,
      DeploymentEntity de) {
    K8Deployment k8Deployment = new K8Deployment(client);
    DeploymentSpec spec = k8Deployment.create(pe, streamIn, streamOut, deploymentSpec, de.getId());
    try {
      de.setSpec(DeploymentSpec.serializeDeploymentSpec(spec.deployment()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return de;
  }

  private UUID getUUIDFromNode(JsonNode node, String key) {
    return UUID.fromString(node.get(key).asText());
  }
}

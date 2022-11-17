package io.datacater.core.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.exceptions.*;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.smallrye.mutiny.Uni;
import java.io.*;
import java.util.List;
import java.util.Map;
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

@Path("/deployments")
@RolesAllowed("dev")
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "apiToken")
@RequestScoped
public class DeploymentEndpoint {
  @Inject Mutiny.SessionFactory sf;

  @Inject KubernetesClient client;

  @GET
  @Path("{uuid}")
  public Uni<DeploymentEntity> getDeployment(@PathParam("uuid") UUID deploymentId) {
    return sf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(this::getK8Deployment);
  }

  @GET
  @Path("{uuid}/logs")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<String> getLogs(@PathParam("uuid") UUID deploymentId) {
    return sf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(deployment -> getDeploymentLogs(deployment.getId()));
  }

  @GET
  @Path("{uuid}/watch-logs")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Uni<Response> watchLogs(
      @PathParam("uuid") UUID deploymentId, @Context Sse sse, @Context SseEventSink eventSink) {
    return sf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            deployment -> {
              try {
                watchLogsRunner(deployment.getId(), sse, eventSink);
              } catch (IOException e) {
                throw new DatacaterException(StringUtilities.wrapString(e.getMessage()));
              }
              return Response.ok().build();
            });
  }

  @GET
  public Uni<List<DeploymentEntity>> getDeployments() {
    return sf.withSession(
        session ->
            session
                .createQuery("from DeploymentEntity", DeploymentEntity.class)
                .getResultList()
                .onItem()
                .transform(this::getK8Deployments));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<DeploymentEntity> createDeployment(DeploymentSpec spec) {
    DeploymentEntity de = new DeploymentEntity(spec);
    Uni<PipelineEntity> pipelineUni = getPipeline(spec);
    return sf.withTransaction(
        (session, transaction) ->
            session
                .persist(de)
                .onItem()
                .transformToUni(empty -> pipelineUni)
                .onItem()
                .transformToUni(
                    pipelineEntity ->
                        Uni.combine()
                            .all()
                            .unis(
                                Uni.createFrom().item(pipelineEntity),
                                getStream(pipelineEntity, StaticConfig.STREAM_IN),
                                getStream(pipelineEntity, StaticConfig.STREAM_OUT))
                            .asTuple())
                .onItem()
                .transform(
                    tuple ->
                        createDeployment(
                            tuple.getItem1(), tuple.getItem3(), tuple.getItem2(), spec, de)));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteDeployment(@PathParam("uuid") UUID deploymentId) {
    return sf.withTransaction(
        ((session, tx) ->
            session
                .find(DeploymentEntity.class, deploymentId)
                .onItem()
                .ifNull()
                .failWith(
                    new DeploymentNotFoundException(
                        StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
                .onItem()
                .ifNotNull()
                .call(
                    de -> {
                      deleteK8Deployment(deploymentId);
                      return session.remove(de);
                    })
                .replaceWith(Response.ok().build())));
  }

  @PUT
  @Path("{uuid}")
  public Uni<DeploymentEntity> updateDeployment(
      @PathParam("uuid") UUID deploymentUuid, DeploymentSpec spec) {
    Uni<PipelineEntity> pipelineUni = getPipeline(spec);
    Uni<DeploymentEntity> deploymentUni = getDeploymentUni(deploymentUuid);
    return pipelineUni
        .onItem()
        .transformToUni(
            pipelineEntity ->
                Uni.combine()
                    .all()
                    .unis(
                        Uni.createFrom().item(pipelineEntity),
                        getStream(pipelineEntity, StaticConfig.STREAM_IN),
                        getStream(pipelineEntity, StaticConfig.STREAM_OUT),
                        deploymentUni)
                    .asTuple())
        .onItem()
        .transform(
            tuple ->
                updateDeployment(
                    tuple.getItem1(), tuple.getItem3(), tuple.getItem2(), spec, tuple.getItem4()))
        .onFailure()
        .transform(
            ex ->
                new UpdateDeploymentException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_UPDATED));
  }

  private Uni<PipelineEntity> getPipeline(DeploymentSpec deploymentSpec) {
    return sf.withTransaction(
        (session, transaction) ->
            session
                .find(PipelineEntity.class, getPipelineUUIDFromMap(deploymentSpec.deployment()))
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(StaticConfig.LoggerMessages.PIPELINE_NOT_FOUND)));
  }

  private Uni<DeploymentEntity> getDeploymentUni(UUID deploymentUuid) {
    return sf.withTransaction(
        (session, transaction) ->
            session
                .find(DeploymentEntity.class, deploymentUuid)
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(
                        StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND)));
  }

  private Uni<StreamEntity> getStream(PipelineEntity pipeline, String key) {
    return sf.withTransaction(
        (session, transaction) ->
            session
                .find(StreamEntity.class, getStreamUUIDFromNode(pipeline.getMetadata(), key))
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(
                        String.format(StaticConfig.LoggerMessages.STREAM_NOT_FOUND, key))));
  }

  private String getDeploymentLogs(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getLogs(deploymentId);
  }

  private RollableScalableResource<Deployment> watchDeploymentLogs(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.watchLogs(deploymentId);
  }

  private List<DeploymentEntity> getK8Deployments(List<DeploymentEntity> deployments) {
    K8Deployment k8Deployment = new K8Deployment(client);
    for (DeploymentEntity deployment : deployments) {
      deployment.setStatus(
          DeploymentEntity.serializeMap(k8Deployment.getDeployment(deployment.getId())));
    }
    return deployments;
  }

  private DeploymentEntity getK8Deployment(DeploymentEntity deployment) {
    K8Deployment k8Deployment = new K8Deployment(client);
    Map<String, Object> map = k8Deployment.getDeployment(deployment.getId());
    JsonNode node = DeploymentEntity.serializeMap(map);
    deployment.setStatus(node);
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
    de.setStatus(
        DeploymentEntity.serializeMap(
            k8Deployment.create(pe, streamIn, streamOut, deploymentSpec, de.getId())));
    return de;
  }

  private DeploymentEntity updateDeployment(
      PipelineEntity pe,
      StreamEntity streamOut,
      StreamEntity streamIn,
      DeploymentSpec deploymentSpec,
      DeploymentEntity de) {
    deleteK8Deployment(de.getId());
    return createDeployment(pe, streamOut, streamIn, deploymentSpec, de);
  }

  private UUID getStreamUUIDFromNode(JsonNode node, String key) {
    try {

      return UUID.fromString(node.get(key).asText());
    } catch (Exception e) {
      throw new CreateDeploymentException(
          String.format(StaticConfig.LoggerMessages.STREAM_NOT_FOUND, key));
    }
  }

  private UUID getPipelineUUIDFromMap(Map<String, Object> map) {
    try {
      return UUID.fromString(map.get(StaticConfig.PIPELINE_NODE_TEXT).toString());
    } catch (Exception e) {
      throw new CreateDeploymentException(StaticConfig.LoggerMessages.PIPELINE_NOT_FOUND);
    }
  }

  private void watchLogsRunner(UUID deploymentId, @Context Sse sse, @Context SseEventSink eventSink)
      throws IOException {
    LogWatch lw =
        watchDeploymentLogs(deploymentId)
            .inContainer(StaticConfig.DEPLOYMENT_NAME_PREFIX + deploymentId)
            .watchLog();
    InputStream is = lw.getOutput();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      final OutboundSseEvent sseEvent = sse.newEventBuilder().data(line).build();

      eventSink.send(sseEvent);
    }
    is.close();
    lw.close();
  }
}

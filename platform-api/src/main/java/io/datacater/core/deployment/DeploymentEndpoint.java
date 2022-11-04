package io.datacater.core.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.api.model.ListMeta;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.smallrye.mutiny.Uni;
import java.io.*;
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
  public Uni<ObjectMeta> getDeployment(@PathParam("uuid") UUID deploymentId) {
    return Uni.createFrom().item(getK8Deployment(deploymentId));
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
  public Uni<ListMeta> getDeployments() {
    return Uni.createFrom().item(getK8Deployments());
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<UUID> createDeployment(DeploymentSpec spec) {
    return apply(spec);
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteDeployment(@PathParam("uuid") UUID deploymentId) {
    deleteK8Deployment(deploymentId);
    return Uni.createFrom().item(Response.ok().build());
  }

  @PUT
  @Path("{uuid}")
  public Uni<UUID> updateDeployment(@PathParam("uuid") UUID deploymentUuid, DeploymentSpec spec) {
    deleteK8Deployment(deploymentUuid);
    return apply(spec);
  }

  private Uni<UUID> apply(DeploymentSpec spec) {
    return sf.withTransaction(
        (session, transaction) ->
            session
                .find(
                    PipelineEntity.class,
                    UUID.fromString(
                        spec.deployment().get(StaticConfig.PIPELINE_NODE_TEXT).toString()))
                .onItem()
                .ifNotNull()
                .transformToUni(pe -> transformPipelineEntity(session, pe, spec)));
  }

  private Uni<UUID> transformPipelineEntity(
      Mutiny.Session session, PipelineEntity pe, DeploymentSpec deploymentSpec) {
    return session
        .find(StreamEntity.class, getUUIDFromNode(pe.getMetadata(), StaticConfig.STREAM_IN))
        .onItem()
        .ifNotNull()
        .transformToUni(streamIn -> transformStreamIn(session, pe, streamIn, deploymentSpec));
  }

  private Uni<UUID> transformStreamIn(
      Mutiny.Session session,
      PipelineEntity pe,
      StreamEntity streamIn,
      DeploymentSpec deploymentSpec) {
    return session
        .find(StreamEntity.class, getUUIDFromNode(pe.getMetadata(), StaticConfig.STREAM_OUT))
        .onItem()
        .ifNotNull()
        .transformToUni(streamOut -> transformStreamOut(pe, streamOut, streamIn, deploymentSpec));
  }

  private Uni<UUID> transformStreamOut(
      PipelineEntity pe,
      StreamEntity streamOut,
      StreamEntity streamIn,
      DeploymentSpec deploymentSpec) {
    return Uni.createFrom().item(createDeployment(pe, streamOut, streamIn, deploymentSpec));
  }

  private String getDeploymentLogs(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getLogs(deploymentId);
  }

  private RollableScalableResource<Deployment> watchDeploymentLogs(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.watchLogs(deploymentId);
  }

  private ListMeta getK8Deployments() {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getDeployments();
  }

  private ObjectMeta getK8Deployment(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getDeployment(deploymentId);
  }

  private void deleteK8Deployment(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    k8Deployment.delete(deploymentId);
  }

  private UUID createDeployment(
      PipelineEntity pe,
      StreamEntity streamOut,
      StreamEntity streamIn,
      DeploymentSpec deploymentSpec) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.create(pe, streamIn, streamOut, deploymentSpec);
  }

  private UUID getUUIDFromNode(JsonNode node, String key) {
    return UUID.fromString(node.get(key).asText());
  }
}

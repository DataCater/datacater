package io.datacater.core.deployment;

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
import org.jboss.logging.Logger;

@Path("/api/alpha/deployments")
@RolesAllowed("dev")
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "apiToken")
@RequestScoped
public class DeploymentEndpoint {
  private static final Logger LOGGER = Logger.getLogger(DeploymentEndpoint.class);
  @Inject Mutiny.SessionFactory sf;

  @Inject KubernetesClient client;

  @GET
  @Path("{deploymentName}")
  public Uni<ObjectMeta> getDeployment(@PathParam("deploymentName") String deploymentName) {
    return Uni.createFrom().item(getK8Deployment(deploymentName));
  }

  @GET
  @Path("{deploymentName}/logs")
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> getLogs(@PathParam("deploymentName") String deploymentName) {
    return Uni.createFrom().item(getDeploymentLogs(deploymentName));
  }

  @GET
  @Path("{deploymentName}/watchLogs")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Response watchLogs(
      @PathParam("deploymentName") String deploymentName,
      @Context Sse sse,
      @Context SseEventSink eventSink)
      throws IOException {
    LogWatch lw = watchDeploymentLogs(deploymentName).watchLog();
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
  @Path("{pipelineUuid}")
  public Uni<String> createDeployment(@PathParam("pipelineUuid") UUID pipelineId) {
    return apply(pipelineId);
  }

  @DELETE
  @Path("{deploymentName}")
  public Uni<Response> deleteDeployment(@PathParam("deploymentName") String deploymentName) {
    return Uni.createFrom().item(deleteK8Deployment(deploymentName));
  }

  @PUT
  @Path("{pipelineUuid}")
  public Uni<String> updateDeployment(@PathParam("pipelineUuid") UUID pipelineId) {
    return apply(pipelineId);
  }

  private Uni<String> apply(UUID pipelineId) {
    return sf.withTransaction(
        (session, transaction) ->
            session
                .find(PipelineEntity.class, pipelineId)
                .onItem()
                .ifNotNull()
                .transformToUni(pe -> transformPipelineEntity(session, pe)));
  }

  private Uni<String> transformPipelineEntity(Mutiny.Session session, PipelineEntity pe) {
    return session
        .find(StreamEntity.class, getUUIDFromNode(pe, StaticConfig.STREAM_IN))
        .onItem()
        .ifNotNull()
        .transformToUni(streamIn -> transformStreamIn(session, pe, streamIn));
  }

  private Uni<String> transformStreamIn(
      Mutiny.Session session, PipelineEntity pe, StreamEntity streamIn) {
    return session
        .find(StreamEntity.class, getUUIDFromNode(pe, StaticConfig.STREAM_OUT))
        .onItem()
        .ifNotNull()
        .transformToUni(streamOut -> transformStreamOut(pe, streamOut, streamIn));
  }

  private Uni<String> transformStreamOut(
      PipelineEntity pe, StreamEntity streamOut, StreamEntity streamIn) {
    return Uni.createFrom().item(createDeployment(pe, streamOut, streamIn));
  }

  private String getDeploymentLogs(String deploymentName) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getLogs(deploymentName);
  }

  private RollableScalableResource<Deployment> watchDeploymentLogs(String deploymentName) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.watchLogs(deploymentName);
  }

  private ListMeta getK8Deployments() {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getDeployments();
  }

  private ObjectMeta getK8Deployment(String deploymentName) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getDeployment(deploymentName);
  }

  private Response deleteK8Deployment(String deploymentName) {
    K8Deployment k8Deployment = new K8Deployment(client);
    k8Deployment.delete(deploymentName);
    return Response.ok().build();
  }

  private String createDeployment(
      PipelineEntity pe, StreamEntity streamOut, StreamEntity streamIn) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.create(pe, streamIn, streamOut);
  }

  private UUID getUUIDFromNode(PipelineEntity pe, String node) {
    return UUID.fromString(pe.getMetadata().get(node).asText());
  }
}

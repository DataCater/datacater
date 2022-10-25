package io.datacater.core.deployment;

import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.api.model.ListMeta;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.smallrye.mutiny.Uni;
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
  @Path("{deploymentName}")
  public Uni<ObjectMeta> getStream(@PathParam("deploymentName") String deploymentName) {
    return Uni.createFrom().item(getK8Deployment(deploymentName));
  }

  @GET
  @Path("{deploymentName}/logs")
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> getLogs(@PathParam("deploymentName") String deploymentName) {
    return Uni.createFrom().item(getDeploymentLogs(deploymentName));
  }

  @GET
  public Uni<ListMeta> getDeployments() {
    return Uni.createFrom().item(getK8Deployments());
  }

  @POST
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<String> createDeployment(DatacaterDeployment datacaterDeployment) {
    return apply(datacaterDeployment);
  }

  @DELETE
  @Path("{deploymentName}")
  public Uni<Response> deleteDeployment(@PathParam("deploymentName") String deploymentName) {
    return Uni.createFrom().item(deleteK8Deployment(deploymentName));
  }

  @PUT
  @Path("{deploymentName}")
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<String> updateDeployment(
      @PathParam("deploymentName") String deploymentName, DatacaterDeployment datacaterDeployment) {
    return apply(datacaterDeployment);
  }

  private Uni<String> apply(DatacaterDeployment datacaterDeployment) {
    return sf.withTransaction(
        (session, transaction) ->
            session
                .find(PipelineEntity.class, datacaterDeployment.spec().getPipelineId())
                .onItem()
                .ifNotNull()
                .transformToUni(pe -> transformPipelineEntity(session, datacaterDeployment, pe)));
  }

  private Uni<String> transformPipelineEntity(
      Mutiny.Session session, DatacaterDeployment datacaterDeployment, PipelineEntity pe) {
    return session
        .find(StreamEntity.class, getUUIDFromNode(pe, StaticConfig.STREAM_IN))
        .onItem()
        .ifNotNull()
        .transformToUni(streamIn -> transformStreamIn(session, datacaterDeployment, pe, streamIn));
  }

  private Uni<String> transformStreamIn(
      Mutiny.Session session,
      DatacaterDeployment datacaterDeployment,
      PipelineEntity pe,
      StreamEntity streamIn) {
    return session
        .find(StreamEntity.class, getUUIDFromNode(pe, StaticConfig.STREAM_OUT))
        .onItem()
        .ifNotNull()
        .transformToUni(
            streamOut -> transformStreamOut(datacaterDeployment, pe, streamOut, streamIn));
  }

  private Uni<String> transformStreamOut(
      DatacaterDeployment datacaterDeployment,
      PipelineEntity pe,
      StreamEntity streamOut,
      StreamEntity streamIn) {
    return Uni.createFrom().item(createDeployment(datacaterDeployment, pe, streamOut, streamIn));
  }

  private String getDeploymentLogs(String deploymentName) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.getLogs(deploymentName);
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
      DatacaterDeployment datacaterDeployment,
      PipelineEntity pe,
      StreamEntity streamOut,
      StreamEntity streamIn) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.create(datacaterDeployment, pe, streamIn, streamOut);
  }

  private UUID getUUIDFromNode(PipelineEntity pe, String node) {
    return UUID.fromString(pe.getMetadata().get(node).asText());
  }
}

package io.datacater.core.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.*;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.jboss.logging.Logger;

@Path("/deployments")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "apiToken")
@RequestScoped
public class DeploymentEndpoint {

  @Inject DataCaterSessionFactory dsf;

  static final Logger LOGGER = Logger.getLogger(DeploymentEndpoint.class);

  @Inject KubernetesClient client;

  @GET
  @Path("{uuid}")
  public Uni<DeploymentEntity> getDeployment(@PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
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
  public Uni<List<String>> getLogs(@PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(Unchecked.function(deployment -> getDeploymentLogsAsList(deployment.getId())));
  }

  @GET
  @Path("{uuid}/health")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> getHealth(@PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            Unchecked.function(
                deployment -> {
                  HttpClient httpClient = HttpClient.newHttpClient();
                  HttpRequest req =
                      buildDeploymentServiceRequest(
                          deploymentId, StaticConfig.EnvironmentVariables.DEPLOYMENT_HEALTH_PATH);
                  HttpResponse<String> response =
                      httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                  return Response.ok().entity(response.body()).build();
                }));
  }

  @GET
  @Path("{uuid}/metrics")
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<Response> getMetrics(@PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            Unchecked.function(
                deployment -> {
                  HttpClient httpClient = HttpClient.newHttpClient();
                  HttpRequest req =
                      buildDeploymentServiceRequest(
                          deploymentId, StaticConfig.EnvironmentVariables.DEPLOYMENT_METRICS_PATH);
                  HttpResponse<String> response =
                      httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                  return Response.ok().entity(response.body()).build();
                }));
  }

  @GET
  @Path("{uuid}/watch-logs")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Uni<Response> watchLogs(
      @PathParam("uuid") UUID deploymentId, @Context Sse sse, @Context SseEventSink eventSink) {
    return dsf.withTransaction(
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
    return dsf.withSession(
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
    Uni<List<ConfigEntity>> configList = ConfigUtilities.getConfig(spec.configSelector(), dsf);

    return dsf.withTransaction(
        (session, transaction) ->
            session
                .persist(de)
                .onItem()
                .transform(
                    voidObject ->
                        configList
                            .onItem()
                            .transform(
                                configs -> ConfigUtilities.applyConfigsToDeployment(spec, configs)))
                .onItem()
                .transformToUni(
                    combinedSpecUni ->
                        combinedSpecUni
                            .onItem()
                            .transformToUni(
                                combinedSpec ->
                                    Uni.combine()
                                        .all()
                                        .unis(
                                            Uni.createFrom().item(combinedSpec),
                                            getPipeline(combinedSpec))
                                        .asTuple()))
                .onItem()
                .transformToUni(
                    specAndPipeline ->
                        Uni.combine()
                            .all()
                            .unis(
                                Uni.createFrom().item(specAndPipeline.getItem2()),
                                getStream(
                                    specAndPipeline.getItem1(),
                                    StaticConfig.STREAM_IN_CONFIG,
                                    specAndPipeline.getItem2(),
                                    StaticConfig.STREAM_IN),
                                getStream(
                                    specAndPipeline.getItem1(),
                                    StaticConfig.STREAM_OUT_CONFIG,
                                    specAndPipeline.getItem2(),
                                    StaticConfig.STREAM_OUT),
                                configList,
                                Uni.createFrom().item(specAndPipeline.getItem1()))
                            .asTuple())
                .onItem()
                .transform(
                    tuple ->
                        createDeployment(
                            tuple.getItem1(),
                            tuple.getItem3(),
                            tuple.getItem2(),
                            tuple.getItem5(),
                            de,
                            tuple.getItem4())));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteDeployment(@PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
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
    Uni<DeploymentEntity> deploymentUni = getDeploymentUni(deploymentUuid);
    Uni<List<ConfigEntity>> configList = ConfigUtilities.getConfig(spec.configSelector(), dsf);
    return configList
        .onItem()
        .transform(configs -> ConfigUtilities.applyConfigsToDeployment(spec, configs))
        .onItem()
        .transformToUni(
            combinedSpec ->
                Uni.combine()
                    .all()
                    .unis(Uni.createFrom().item(combinedSpec), getPipeline(combinedSpec))
                    .asTuple())
        .onItem()
        .transformToUni(
            specAndPipeline ->
                Uni.combine()
                    .all()
                    .unis(
                        Uni.createFrom().item(specAndPipeline.getItem2()),
                        getStream(
                            specAndPipeline.getItem1(),
                            StaticConfig.STREAM_IN_CONFIG,
                            specAndPipeline.getItem2(),
                            StaticConfig.STREAM_IN),
                        getStream(
                            specAndPipeline.getItem1(),
                            StaticConfig.STREAM_OUT_CONFIG,
                            specAndPipeline.getItem2(),
                            StaticConfig.STREAM_OUT),
                        deploymentUni,
                        configList,
                        Uni.createFrom().item(specAndPipeline.getItem1()))
                    .asTuple())
        .onItem()
        .transform(
            tuple ->
                updateDeployment(
                    tuple.getItem1(),
                    tuple.getItem3(),
                    tuple.getItem2(),
                    tuple.getItem6(),
                    tuple.getItem4(),
                    tuple.getItem5()))
        .onFailure()
        .transform(
            ex ->
                new UpdateDeploymentException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_UPDATED));
  }

  private Uni<PipelineEntity> getPipeline(DeploymentSpec deploymentSpec) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(PipelineEntity.class, getPipelineUUIDFromMap(deploymentSpec.deployment()))
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(StaticConfig.LoggerMessages.PIPELINE_NOT_FOUND)));
  }

  private Uni<DeploymentEntity> getDeploymentUni(UUID deploymentUuid) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(DeploymentEntity.class, deploymentUuid)
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(
                        StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND)));
  }

  private Uni<StreamEntity> getStream(
      DeploymentSpec spec, String deploymentSpecKey, PipelineEntity pipeline, String key) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(
                    StreamEntity.class,
                    getStreamUUID(spec, deploymentSpecKey, pipeline.getMetadata(), key))
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(
                        String.format(StaticConfig.LoggerMessages.STREAM_NOT_FOUND, key))));
  }

  private List<String> getDeploymentLogsAsList(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return Arrays.asList(k8Deployment.getLogs(deploymentId).split("\n"));
  }

  private HttpRequest buildDeploymentServiceRequest(UUID deploymentId, String path) {
    K8Deployment k8Deployment = new K8Deployment(client);
    String clusterIp = k8Deployment.getClusterIp(deploymentId);
    String uriReady =
        String.format(
            "%s://%s:%d%s",
            StaticConfig.EnvironmentVariables.DEPLOYMENT_CONTAINER_PROTOCOL,
            clusterIp,
            StaticConfig.EnvironmentVariables.DEPLOYMENT_CONTAINER_PORT,
            path);

    return HttpRequest.newBuilder()
        .GET()
        .version(HttpClient.Version.HTTP_1_1)
        .uri(URI.create(uriReady))
        .build();
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
    try {
      K8Deployment k8Deployment = new K8Deployment(client);
      k8Deployment.delete(deploymentId);
    } catch (DeploymentNotFoundException e) {
      LOGGER.error(
          String.format("Could not find Kubernetes deployment with id %s", deploymentId.toString()),
          e);
    }
  }

  private DeploymentEntity createDeployment(
      PipelineEntity pe,
      StreamEntity streamOut,
      StreamEntity streamIn,
      DeploymentSpec deploymentSpec,
      DeploymentEntity de,
      List<ConfigEntity> configList) {
    K8Deployment k8Deployment = new K8Deployment(client);
    deploymentSpec = ConfigUtilities.applyConfigsToDeployment(deploymentSpec, configList);
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
      DeploymentEntity de,
      List<ConfigEntity> configList) {
    deleteK8Deployment(de.getId());
    return createDeployment(pe, streamOut, streamIn, deploymentSpec, de, configList);
  }

  private UUID getStreamUUID(
      DeploymentSpec spec, String deploymentSpecKey, JsonNode node, String key) {
    // try and get from deploymentSpec
    String uuidString = "";
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> streamConfig =
        mapper.convertValue(spec.deployment().get(deploymentSpecKey), Map.class);
    if (streamConfig != null && streamConfig.get("uuid") != null) {
      uuidString = streamConfig.get("uuid").toString();
    }

    // if not in deploymentSpec, try and get from pipeline
    if ((uuidString == null || uuidString.isEmpty() || uuidString.isBlank())
        && node.get(key) != null) {
      uuidString = node.get(key).asText();
    }

    // if in neither, throw exception
    if (uuidString == null || uuidString.isEmpty() || uuidString.isBlank()) {
      throw new CreateDeploymentException(
          String.format(StaticConfig.LoggerMessages.STREAM_NOT_FOUND, key));
    }

    return UUID.fromString(uuidString);
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

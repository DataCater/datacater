package io.datacater.core.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.*;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.Stream;
import io.datacater.core.stream.StreamEntity;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ContainerResource;
import io.fabric8.kubernetes.client.dsl.LogWatch;
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
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
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
        .failWith(
            new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND));
  }

  @GET
  @Path("{uuid}/logs")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<List<String>> getLogs(
      @PathParam("uuid") UUID deploymentId, @DefaultValue("1") @QueryParam("replica") int replica) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            Unchecked.function(deployment -> getDeploymentLogsAsList(deployment.getId(), replica)));
  }

  @GET
  @Path("{uuid}/health")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> getHealth(
      @PathParam("uuid") UUID deploymentId, @DefaultValue("1") @QueryParam("replica") int replica) {
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
                          deploymentId,
                          StaticConfig.EnvironmentVariables.DEPLOYMENT_HEALTH_PATH,
                          replica);
                  HttpResponse<String> response =
                      httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                  return Response.ok().entity(response.body()).build();
                }));
  }

  @GET
  @Path("{uuid}/metrics")
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<Response> getMetrics(
      @PathParam("uuid") UUID deploymentId, @DefaultValue("1") @QueryParam("replica") int replica) {
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
                          deploymentId,
                          StaticConfig.EnvironmentVariables.DEPLOYMENT_METRICS_PATH,
                          replica);
                  HttpResponse<String> response =
                      httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                  return Response.ok().entity(response.body()).build();
                }));
  }

  @GET
  @Path("{uuid}/status")
  public Uni<DataCaterDeploymentStatus> getStatusByUuid(@PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            deploymentEntity -> {
              Deployment deployment = new K8Deployment(client).getDeploymentObject(deploymentId);
              return DataCaterDeploymentStatus.from(deployment);
            });
  }

  @GET
  @Path("{uuid}/watch-logs")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Uni<Response> watchLogs(
      @PathParam("uuid") UUID deploymentId,
      @DefaultValue("1") @QueryParam("replica") int replica,
      @Context Sse sse,
      @Context SseEventSink eventSink) {
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
                watchLogsRunner(deployment.getId(), replica, sse, eventSink);
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
            session.createQuery("from DeploymentEntity", DeploymentEntity.class).getResultList());
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<DeploymentEntity> createDeployment(DeploymentSpec spec) {
    DeploymentEntity de = new DeploymentEntity(spec);

    return dsf.withTransaction(
        (session, transaction) ->
            session
                .persist(de)
                .onItem()
                .transformToUni(
                    entity ->
                        Uni.combine()
                            .all()
                            .unis(
                                ConfigUtilities.getMappedConfigs(spec.configSelector(), session),
                                Uni.createFrom().item(entity))
                            .asTuple())
                .onItem()
                .transformToUni(
                    tuple -> {
                      DeploymentSpec combinedSpec =
                          ConfigUtilities.applyConfigsToDeployment(spec, tuple.getItem1());
                      return Uni.combine()
                          .all()
                          .unis(
                              Uni.createFrom().item(combinedSpec),
                              getPipeline(combinedSpec),
                              Uni.createFrom().item(tuple.getItem1()))
                          .asTuple();
                    })
                .onItem()
                .transformToUni(
                    tuple ->
                        Uni.combine()
                            .all()
                            .unis(
                                Uni.createFrom().item(tuple.getItem2()),
                                getStream(
                                    tuple.getItem1(),
                                    StaticConfig.STREAM_IN_CONFIG,
                                    tuple.getItem2(),
                                    StaticConfig.STREAM_IN),
                                getStream(
                                    tuple.getItem1(),
                                    StaticConfig.STREAM_OUT_CONFIG,
                                    tuple.getItem2(),
                                    StaticConfig.STREAM_OUT),
                                Uni.createFrom().item(tuple.getItem3()),
                                Uni.createFrom().item(tuple.getItem1()))
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
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<DeploymentEntity> updateDeployment(
      @PathParam("uuid") UUID deploymentUuid, DeploymentSpec spec) {
    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .find(DeploymentEntity.class, deploymentUuid)
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
                .onItem()
                .transformToUni(
                    entity ->
                        Uni.combine()
                            .all()
                            .unis(
                                ConfigUtilities.getMappedConfigs(spec.configSelector(), session),
                                Uni.createFrom().item(entity))
                            .asTuple())
                .onItem()
                .transformToUni(
                    tuple -> {
                      DeploymentSpec combinedSpec =
                          ConfigUtilities.applyConfigsToDeployment(
                              DeploymentSpec.from(spec), tuple.getItem1());
                      return Uni.combine()
                          .all()
                          .unis(
                              Uni.createFrom().item(combinedSpec),
                              getPipeline(combinedSpec),
                              Uni.createFrom().item(tuple.getItem1()),
                              Uni.createFrom().item(tuple.getItem2()))
                          .asTuple();
                    })
                .onItem()
                .transformToUni(
                    tuple ->
                        Uni.combine()
                            .all()
                            .unis(
                                Uni.createFrom().item(tuple.getItem2()),
                                getStream(
                                    tuple.getItem1(),
                                    StaticConfig.STREAM_IN_CONFIG,
                                    tuple.getItem2(),
                                    StaticConfig.STREAM_IN),
                                getStream(
                                    tuple.getItem1(),
                                    StaticConfig.STREAM_OUT_CONFIG,
                                    tuple.getItem2(),
                                    StaticConfig.STREAM_OUT),
                                Uni.createFrom().item(tuple.getItem4()),
                                Uni.createFrom().item(tuple.getItem3()),
                                Uni.createFrom().item(tuple.getItem1()))
                            .asTuple())
                .onItem()
                .transform(
                    tuple -> {
                      updateDeployment(
                          tuple.getItem1(),
                          tuple.getItem3(),
                          tuple.getItem2(),
                          tuple.getItem6(),
                          tuple.getItem4(),
                          tuple.getItem5());
                      return session.merge(tuple.getItem4().updateEntity(spec));
                    })
                .flatMap(entity -> entity)
                .onFailure()
                .transform(
                    ex ->
                        new UpdateDeploymentException(
                            StaticConfig.LoggerMessages.DEPLOYMENT_NOT_UPDATED))));
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

  private Uni<Stream> getStream(
      DeploymentSpec spec, String deploymentSpecKey, PipelineEntity pipeline, String key) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(
                    StreamEntity.class,
                    getStreamUUID(spec, deploymentSpecKey, pipeline.getMetadata(), key))
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
                            .unis(Uni.createFrom().item(stream), configList)
                            .asTuple();
                      } catch (JsonProcessingException ex) {
                        throw new DatacaterException(ex.getMessage());
                      }
                    })
                .onItem()
                .ifNotNull()
                .transform(
                    tuple -> {
                      Stream stream = tuple.getItem1();
                      stream = ConfigUtilities.applyConfigsToStream(stream, tuple.getItem2());
                      return stream;
                    })
                .onItem()
                .ifNull()
                .failWith(
                    new CreateDeploymentException(
                        String.format(StaticConfig.LoggerMessages.STREAM_NOT_FOUND, key))));
  }

  private List<String> getDeploymentLogsAsList(UUID deploymentId, int replica) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return Arrays.asList(k8Deployment.getLogs(deploymentId, replica).split("\n"));
  }

  private HttpRequest buildDeploymentServiceRequest(UUID deploymentId, String path, int replica) {
    K8Deployment k8Deployment = new K8Deployment(client);
    String ip = k8Deployment.getDeploymentReplicaIp(deploymentId, replica).replace(".", "-");
    String namespace = StaticConfig.EnvironmentVariables.NAMESPACE;
    int port = StaticConfig.EnvironmentVariables.DEPLOYMENT_CONTAINER_PORT;
    String protocol = StaticConfig.EnvironmentVariables.DEPLOYMENT_CONTAINER_PROTOCOL;

    String uriReady =
        String.format("%s://%s.%s.pod.cluster.local:%d%s", protocol, ip, namespace, port, path);

    return HttpRequest.newBuilder()
        .GET()
        .version(HttpClient.Version.HTTP_1_1)
        .uri(URI.create(uriReady))
        .build();
  }

  private ContainerResource watchDeploymentLogs(UUID deploymentId, int replica) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.watchLogs(deploymentId, replica);
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
      Stream streamOut,
      Stream streamIn,
      DeploymentSpec deploymentSpec,
      DeploymentEntity de,
      List<ConfigEntity> configList) {
    K8Deployment k8Deployment = new K8Deployment(client);
    DeploymentSpec specWithConfig =
        ConfigUtilities.applyConfigsToDeployment(DeploymentSpec.from(deploymentSpec), configList);

    k8Deployment.create(pe, streamIn, streamOut, specWithConfig, de.getId());

    return de;
  }

  private DeploymentEntity updateDeployment(
      PipelineEntity pe,
      Stream streamOut,
      Stream streamIn,
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

  private void watchLogsRunner(
      UUID deploymentId, int replica, @Context Sse sse, @Context SseEventSink eventSink)
      throws IOException {
    LogWatch lw = watchDeploymentLogs(deploymentId, replica).watchLog();
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

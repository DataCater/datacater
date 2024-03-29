package io.datacater.core.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.exceptions.DatacaterException;
import io.datacater.core.exceptions.PipelineNotFoundException;
import io.datacater.core.kubernetes.DataCaterK8sConfig;
import io.datacater.core.kubernetes.PythonRunnerPool;
import io.datacater.core.kubernetes.PythonRunnerPool.NamedPod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.logging.Logger;

@Path("/pipelines")
@Authenticated
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
@SecurityRequirement(name = "apiToken")
public class PipelineEndpoint {

  @Inject PythonRunnerPool runnerPool;
  static final Logger LOGGER = Logger.getLogger(PipelineEndpoint.class);
  @Inject PipelineUtilities pipelineUtil;
  @Inject KubernetesClient kubernetesClient;
  @Inject DataCaterSessionFactory dsf;
  WebClient client;

  public PipelineEndpoint(Vertx vertx) {
    this.client = WebClient.create(vertx);
  }

  @GET
  public Uni<List<PipelineEntity>> getPipelines() {
    return dsf.withSession(
        session ->
            session.createQuery("from PipelineEntity", PipelineEntity.class).getResultList());
  }

  @GET
  @Path("{uuid}")
  public Uni<PipelineEntity> getPipeline(@PathParam("uuid") UUID uuid) {
    return dsf.withTransaction(((session, transaction) -> session.find(PipelineEntity.class, uuid)))
        .onItem()
        .ifNull()
        .failWith(new PipelineNotFoundException(StaticConfig.PIPELINE_NOT_FOUND));
  }

  @POST
  @RequestBody
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<PipelineEntity> createPipeline(Pipeline pipeline) throws JsonProcessingException {
    PipelineEntity pe =
        PipelineEntity.from(
            pipeline.getName(),
            pipeline.getSerializedMetadata(),
            PipelineSpec.serializePipelineSpec(pipeline.getSpec().getSteps()));
    return dsf.withTransaction((session, transaction) -> session.persist(pe)).replaceWith(pe);
  }

  @PUT
  @Path("{uuid}")
  @RequestBody
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<PipelineEntity> updatePipeline(@PathParam("uuid") UUID uuid, Pipeline pipeline) {
    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .find(PipelineEntity.class, uuid)
                .call(pe -> session.merge((pe).updateEntity(pipeline)))));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deletePipeline(@PathParam("uuid") UUID uuid) {
    return dsf.withTransaction(
        ((session, tx) ->
            session
                .find(PipelineEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .call(session::remove)
                .replaceWith(Response.ok().build())));
  }

  @POST
  @Path("preview")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> preview(String payload) {
    LOGGER.debug(payload);
    HttpClient httpClient = HttpClient.newHttpClient();
    Uni<NamedPod> namedPod = runnerPool.getPod();

    return namedPod
        .onItem()
        .transform(
            Unchecked.function(
                pod -> {
                  HttpRequest preview = pod.buildPost(payload, StaticConfig.PREVIEW_PATH);
                  HttpResponse<String> previewSend =
                      httpClient.send(preview, BodyHandlers.ofString());

                  kubernetesClient.pods().delete(pod.pod());

                  return Response.ok().entity(previewSend.body()).build();
                }))
        .ifNoItem()
        .after(Duration.ofMillis(DataCaterK8sConfig.PYTHON_RUNNER_PREVIEW_TIMEOUT))
        .failWith(
            () ->
                new DatacaterException(
                    String.format(
                        StaticConfig.FormattedMessages.PYTHON_RUNNER_TIMEOUT_FORMATTED_MSG,
                        DataCaterK8sConfig.PYTHON_RUNNER_PREVIEW_TIMEOUT)));
  }

  @GET
  @Path("{uuid}/inspect")
  public Uni<String> inspect(@PathParam("uuid") UUID uuid) {
    return pipelineUtil.transformMessages(uuid);
  }
}

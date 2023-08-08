package io.datacater.core.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.exceptions.DatacaterException;
import io.datacater.core.exceptions.PipelineNotFoundException;
import io.datacater.core.kubernetes.DataCaterK8sConfig;
import io.datacater.core.kubernetes.PythonRunnerPool;
import io.datacater.core.kubernetes.PythonRunnerPool.NamedPod;
import io.datacater.core.project.ProjectUtilities;
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

@Path("{project}/pipelines")
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
  @Inject ProjectUtilities projectUtil;

  public PipelineEndpoint(Vertx vertx) {
    this.client = WebClient.create(vertx);
  }

  @GET
  public Uni<List<PipelineEntity>> getPipelines(@PathParam("project") String project) {
    return dsf.withSession(
        session ->
            session
                .createQuery("from PipelineEntity", PipelineEntity.class)
                .getResultList()
                .onItem()
                .ifNull()
                .continueWith(List.of())
                .onItem()
                .ifNotNull()
                .transform(
                    list ->
                        list.stream().filter(item -> item.getProject().equals(project)).toList())
                .onFailure()
                .recoverWithItem(List.of()));
  }

  @GET
  @Path("{uuid}")
  public Uni<PipelineEntity> getPipeline(
      @PathParam("project") String project, @PathParam("uuid") UUID uuid) {
    return dsf.withTransaction(((session, transaction) -> session.find(PipelineEntity.class, uuid)))
        .onItem()
        .ifNull()
        .failWith(new PipelineNotFoundException(StaticConfig.PIPELINE_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            item -> {
              if (item.getProject().equals(project)) {
                return item;
              }
              return null;
            });
  }

  @POST
  @RequestBody
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<PipelineEntity> createPipeline(@PathParam("project") String project, Pipeline pipeline)
      throws JsonProcessingException {
    PipelineEntity pe =
        PipelineEntity.from(
            pipeline.getName(),
            pipeline.getSerializedMetadata(),
            PipelineSpec.serializePipelineSpec(pipeline.getSpec().getSteps()),
            project);
    Uni<PipelineEntity> persistResponse =
        dsf.withTransaction((session, transaction) -> session.persist(pe)).replaceWith(pe);

    return projectUtil.findProjectAndPersist(project, persistResponse);
  }

  @PUT
  @Path("{uuid}")
  @RequestBody
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<PipelineEntity> updatePipeline(
      @PathParam("project") String project, @PathParam("uuid") UUID uuid, Pipeline pipeline) {
    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .find(PipelineEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .transform(
                    item -> {
                      if (item.getProject().equals(project)) {
                        return item;
                      }
                      return null;
                    })
                .call(pe -> session.merge((pe).updateEntity(pipeline)))));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deletePipeline(
      @PathParam("project") String project, @PathParam("uuid") UUID uuid) {
    return dsf.withTransaction(
        ((session, tx) ->
            session
                .find(PipelineEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .transform(
                    item -> {
                      if (item.getProject().equals(project)) {
                        return item;
                      }
                      return null;
                    })
                .onItem()
                .ifNotNull()
                .call(session::remove)
                .replaceWith(Response.ok().build())));
  }

  @POST
  @Path("preview")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> preview(@PathParam("project") String project, String payload) {
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
  public Uni<String> inspect(@PathParam("project") String project, @PathParam("uuid") UUID uuid) {
    return pipelineUtil.transformMessages(uuid, project);
  }
}

package io.datacater.core.deployment;

import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.*;
import io.datacater.core.pipeline.PipelineUtilities;
import io.datacater.core.stream.StreamUtilities;
import io.datacater.core.utilities.LoggerUtilities;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.logging.Logger;

@Path("{project}/deployments")
@Authenticated
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
@SecurityRequirement(name = "apiToken")
@RequestScoped
public class DeploymentEndpoint {
  @Inject DataCaterSessionFactory dsf;
  @Inject DeploymentUtilities deploymentsUtil;
  @Inject StreamUtilities streamUtil;
  @Inject PipelineUtilities pipelineUtil;
  static final Logger LOGGER = Logger.getLogger(DeploymentEndpoint.class);
  @Inject KubernetesClient client;

  @GET
  @Path("{uuid}")
  public Uni<DeploymentEntity> getDeployment(
      @PathParam("project") String project, @PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
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

  @GET
  @Path("{uuid}/logs")
  public Uni<List<String>> getLogs(
      @PathParam("project") String project,
      @PathParam("uuid") UUID deploymentId,
      @DefaultValue("1") @QueryParam("replica") int replica,
      @DefaultValue("100") @QueryParam("tailingLines") int tailingLines) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
        .onItem()
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
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
        .transform(
            Unchecked.function(
                deployment ->
                    deploymentsUtil.getDeploymentLogsAsList(
                        deployment.getId(), replica, tailingLines)));
  }

  @GET
  @Path("{uuid}/health")
  public Uni<Response> getHealth(
      @PathParam("project") String project,
      @PathParam("uuid") UUID deploymentId,
      @DefaultValue("1") @QueryParam("replica") int replica) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
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
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            Unchecked.function(
                deployment -> {
                  HttpClient httpClient = HttpClient.newHttpClient();
                  HttpRequest req =
                      deploymentsUtil.buildDeploymentServiceRequest(
                          deploymentId,
                          StaticConfig.EnvironmentVariables.DEPLOYMENT_HEALTH_PATH,
                          replica);
                  HttpResponse<String> response =
                      httpClient.send(req, HttpResponse.BodyHandlers.ofString());

                  if (response.statusCode()
                      >= Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                    throw new UnhealthyDeploymentException(response.body());
                  }

                  return Response.ok().entity(response.body()).build();
                }));
  }

  @GET
  @Path("{uuid}/metrics")
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<Response> getMetrics(
      @PathParam("project") String project,
      @PathParam("uuid") UUID deploymentId,
      @DefaultValue("1") @QueryParam("replica") int replica) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
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
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            Unchecked.function(
                deployment -> {
                  HttpClient httpClient = HttpClient.newHttpClient();
                  HttpRequest req =
                      deploymentsUtil.buildDeploymentServiceRequest(
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
  public Uni<DataCaterDeploymentStatus> getStatusByUuid(
      @PathParam("project") String project, @PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
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
      @PathParam("project") String project,
      @PathParam("uuid") UUID deploymentId,
      @DefaultValue("1") @QueryParam("replica") int replica,
      @Context Sse sse,
      @Context SseEventSink eventSink) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(DeploymentEntity.class, deploymentId)))
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
        .ifNull()
        .failWith(new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            deployment -> {
              try {
                deploymentsUtil.watchLogsRunner(deployment.getId(), replica, sse, eventSink);
              } catch (IOException e) {
                throw new DatacaterException(StringUtilities.wrapString(e.getMessage()));
              }
              return Response.ok().build();
            });
  }

  @GET
  public Uni<List<DeploymentEntity>> getDeployments(
      @PathParam("project") String project,
      @QueryParam("in-cluster") @DefaultValue("false") boolean inCluster) {
    if (!inCluster) {
      return dsf.withSession(
              session ->
                  session
                      .createQuery("from DeploymentEntity", DeploymentEntity.class)
                      .getResultList())
          .onItem()
          .ifNull()
          .continueWith(List.of())
          .onItem()
          .ifNotNull()
          .transform(
              list -> list.stream().filter(item -> item.getProject().equals(project)).toList());
    }

    return dsf.withTransaction(
            (session, transaction) ->
                session
                    .createQuery("from DeploymentEntity", DeploymentEntity.class)
                    .getResultList())
        .onItem()
        .ifNull()
        .continueWith(List.of())
        .onItem()
        .ifNotNull()
        .transform(list -> list.stream().filter(item -> item.getProject().equals(project)).toList())
        .onItem()
        .transform(deploymentsUtil::mergeDeploymentEntitiesWithCluster);
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<DeploymentEntity> createDeployment(
      @PathParam("project") String project, DeploymentSpec spec) {
    DeploymentEntity de = new DeploymentEntity(spec, project);

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
                              pipelineUtil.getPipelineFromDeployment(combinedSpec),
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
                                streamUtil.getStreamFromDeployment(
                                    tuple.getItem1(),
                                    StaticConfig.STREAM_IN_CONFIG,
                                    tuple.getItem2(),
                                    StaticConfig.STREAM_IN),
                                streamUtil.getStreamFromDeployment(
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
                        deploymentsUtil.createDeployment(
                            tuple.getItem1(),
                            tuple.getItem3(),
                            tuple.getItem2(),
                            tuple.getItem5(),
                            de,
                            tuple.getItem4())));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteDeployment(
      @PathParam("project") String project, @PathParam("uuid") UUID deploymentId) {
    return dsf.withTransaction(
        ((session, tx) ->
            session
                .find(DeploymentEntity.class, deploymentId)
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
                .ifNull()
                .failWith(
                    new DeploymentNotFoundException(
                        StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND))
                .onItem()
                .ifNotNull()
                .call(
                    de -> {
                      deploymentsUtil.deleteK8Deployment(deploymentId);
                      return session.remove(de);
                    })
                .replaceWith(Response.ok().build())));
  }

  @PUT
  @Path("{uuid}")
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<DeploymentEntity> updateDeployment(
      @PathParam("project") String project,
      @PathParam("uuid") UUID deploymentUuid,
      DeploymentSpec spec) {
    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .find(DeploymentEntity.class, deploymentUuid)
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
                              pipelineUtil.getPipelineFromDeployment(combinedSpec),
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
                                streamUtil.getStreamFromDeployment(
                                    tuple.getItem1(),
                                    StaticConfig.STREAM_IN_CONFIG,
                                    tuple.getItem2(),
                                    StaticConfig.STREAM_IN),
                                streamUtil.getStreamFromDeployment(
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
                      deploymentsUtil.updateDeployment(
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
                    ex -> {
                      LoggerUtilities.logExceptionMessage(
                          LOGGER,
                          new Throwable().getStackTrace()[0].getMethodName(),
                          ex.getMessage());
                      throw new UpdateDeploymentException(
                          StaticConfig.LoggerMessages.DEPLOYMENT_NOT_UPDATED);
                    })));
  }
}

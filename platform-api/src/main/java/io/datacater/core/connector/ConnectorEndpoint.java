package io.datacater.core.connector;

import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.*;
import io.datacater.core.stream.StreamUtilities;
import io.datacater.core.utilities.LoggerUtilities;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
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

@Path("/connectors")
@Authenticated
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
@SecurityRequirement(name = "apiToken")
@RequestScoped
public class ConnectorEndpoint {

  @Inject DataCaterSessionFactory dsf;
  @Inject ConnectorUtilities connectorsUtil;
  @Inject StreamUtilities streamUtil;
  static final Logger LOGGER = Logger.getLogger(ConnectorEndpoint.class);

  @Inject KubernetesClient client;

  @GET
  @Path("{uuid}")
  public Uni<ConnectorEntity> getConnector(@PathParam("uuid") UUID connectorId) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(ConnectorEntity.class, connectorId)))
        .onItem()
        .ifNull()
        .failWith(new ConnectorNotFoundException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND));
  }

  @GET
  public Uni<List<ConnectorEntity>> getConnectors() {
    return dsf.withSession(
        session ->
            session.createQuery("from ConnectorEntity", ConnectorEntity.class).getResultList());
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<ConnectorEntity> createConnector(ConnectorSpec spec) {
    ConnectorEntity connectorEntity = new ConnectorEntity(spec);

    return dsf.withTransaction(
        (session, transaction) ->
            session
                .persist(connectorEntity)
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
                      ConnectorSpec combinedSpec =
                          ConfigUtilities.applyConfigsToConnector(spec, tuple.getItem1());
                      return Uni.combine()
                          .all()
                          .unis(
                              Uni.createFrom().item(combinedSpec),
                              streamUtil.getStreamFromConnector(combinedSpec),
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
                                Uni.createFrom().item(tuple.getItem3()),
                                Uni.createFrom().item(tuple.getItem1()))
                            .asTuple())
                .onItem()
                .transform(
                    tuple ->
                        connectorsUtil.createK8Deployment(
                            tuple.getItem1(),
                            tuple.getItem3(),
                            connectorEntity,
                            tuple.getItem2())));
  }

  @GET
  @Path("{uuid}/logs")
  public Uni<List<String>> getLogs(
      @PathParam("uuid") UUID connectorId,
      @DefaultValue("100") @QueryParam("tailingLines") int tailingLines) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(ConnectorEntity.class, connectorId)))
        .onItem()
        .ifNull()
        .failWith(new ConnectorNotFoundException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            Unchecked.function(
                connector ->
                    connectorsUtil.getConnectorLogsAsList(connector.getId(), tailingLines)));
  }

  @GET
  @Path("{uuid}/watch-logs")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Uni<Response> watchLogs(
      @PathParam("uuid") UUID connectorId, @Context Sse sse, @Context SseEventSink eventSink) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(ConnectorEntity.class, connectorId)))
        .onItem()
        .ifNull()
        .failWith(new ConnectorNotFoundException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            deployment -> {
              try {
                connectorsUtil.watchLogsRunner(deployment.getId(), sse, eventSink);
              } catch (IOException e) {
                throw new DatacaterException(StringUtilities.wrapString(e.getMessage()));
              }
              return Response.ok().build();
            });
  }

  @GET
  @Path("{uuid}/health")
  public Uni<Response> getHealth(@PathParam("uuid") UUID connectorId) {
    return dsf.withTransaction(
            ((session, transaction) -> session.find(ConnectorEntity.class, connectorId)))
        .onItem()
        .ifNull()
        .failWith(new ConnectorNotFoundException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND))
        .onItem()
        .ifNotNull()
        .transform(
            Unchecked.function(
                deployment -> {
                  HttpClient httpClient = HttpClient.newHttpClient();
                  HttpRequest req = connectorsUtil.buildConnectorServiceRequest(connectorId);
                  HttpResponse<String> response =
                      httpClient.send(req, HttpResponse.BodyHandlers.ofString());

                  if (response.statusCode()
                      > Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                    throw new UnhealthyConnectorException(response.body());
                  }

                  return Response.ok().entity(response.body()).build();
                }));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteConnector(@PathParam("uuid") UUID connectorId) {
    return dsf.withTransaction(
        ((session, tx) ->
            session
                .find(ConnectorEntity.class, connectorId)
                .onItem()
                .ifNull()
                .failWith(
                    new ConnectorNotFoundException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND))
                .onItem()
                .ifNotNull()
                .call(
                    connectorEntity -> {
                      connectorsUtil.deleteK8Deployment(connectorId);
                      return session.remove(connectorEntity);
                    })
                .replaceWith(Response.ok().build())));
  }

  @PUT
  @Path("{uuid}")
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<ConnectorEntity> updateConnector(
      @PathParam("uuid") UUID connectorUuid, ConnectorSpec spec) {
    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .find(ConnectorEntity.class, connectorUuid)
                .onItem()
                .ifNull()
                .failWith(
                    new CreateConnectorException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND))
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
                      ConnectorSpec combinedSpec =
                          ConfigUtilities.applyConfigsToConnector(
                              ConnectorSpec.from(spec), tuple.getItem1());
                      return Uni.combine()
                          .all()
                          .unis(
                              Uni.createFrom().item(combinedSpec),
                              streamUtil.getStreamFromConnector(combinedSpec),
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
                                Uni.createFrom().item(tuple.getItem4()),
                                Uni.createFrom().item(tuple.getItem3()),
                                Uni.createFrom().item(tuple.getItem1()))
                            .asTuple())
                .onItem()
                .transform(
                    tuple -> {
                      connectorsUtil.updateK8Deployment(
                          tuple.getItem1(), tuple.getItem4(), tuple.getItem2(), tuple.getItem3());
                      return session.merge(tuple.getItem2().updateEntity(spec));
                    })
                .flatMap(entity -> entity)
                .onFailure()
                .transform(
                    ex -> {
                      LoggerUtilities.logExceptionMessage(
                          LOGGER,
                          new Throwable().getStackTrace()[0].getMethodName(),
                          ex.getMessage());
                      throw new UpdateConnectorException(
                          StaticConfig.LoggerMessages.CONNECTOR_NOT_UPDATED);
                    })));
  }
}

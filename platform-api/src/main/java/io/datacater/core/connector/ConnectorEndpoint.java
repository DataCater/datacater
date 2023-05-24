package io.datacater.core.connector;

import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.ConnectorNotFoundException;
import io.datacater.core.exceptions.CreateConnectorException;
import io.datacater.core.exceptions.DeploymentNotFoundException;
import io.datacater.core.exceptions.UpdateConnectorException;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.logging.Logger;

@Path("/connectors")
@Authenticated
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
@SecurityRequirement(name = "apiToken")
@RequestScoped
public class ConnectorEndpoint {

  @Inject DataCaterSessionFactory dsf;

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
    ConnectorEntity ce = new ConnectorEntity(spec);

    return dsf.withTransaction(
        (session, transaction) ->
            session
                .persist(ce)
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
                              getStream(combinedSpec),
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
                        createConnector(tuple.getItem1(), tuple.getItem3(), ce, tuple.getItem2())));
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
                    ce -> {
                      deleteK8Deployment(connectorId);
                      return session.remove(ce);
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
                              getStream(combinedSpec),
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
                      updateConnector(
                          tuple.getItem1(), tuple.getItem4(), tuple.getItem2(), tuple.getItem3());
                      return session.merge(tuple.getItem2().updateEntity(spec));
                    })
                .flatMap(entity -> entity)
                .onFailure()
                .transform(
                    ex ->
                        new UpdateConnectorException(
                            StaticConfig.LoggerMessages.CONNECTOR_NOT_UPDATED))));
  }

  private Uni<StreamEntity> getStream(ConnectorSpec connectorSpec) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(StreamEntity.class, getStreamUUIDFromMap(connectorSpec.connector()))
                .onItem()
                .ifNull()
                .failWith(
                    new CreateConnectorException(StaticConfig.LoggerMessages.STREAM_NOT_FOUND)));
  }

  private Uni<ConnectorEntity> getConnectorUni(UUID connectorUuid) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(ConnectorEntity.class, connectorUuid)
                .onItem()
                .ifNull()
                .failWith(
                    new CreateConnectorException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND)));
  }

  private void deleteK8Deployment(UUID connectorId) {
    try {
      K8Deployment k8Deployment = new K8Deployment(client);
      k8Deployment.delete(connectorId);
    } catch (DeploymentNotFoundException e) {
      LOGGER.error(
          String.format("Could not find Kubernetes Deployment with id %s", connectorId.toString()),
          e);
    }
  }

  private ConnectorEntity createConnector(
      StreamEntity se,
      ConnectorSpec connectorSpec,
      ConnectorEntity ce,
      List<ConfigEntity> configList) {
    K8Deployment k8Deployment = new K8Deployment(client);
    ConnectorSpec specWithConfig =
        ConfigUtilities.applyConfigsToConnector(ConnectorSpec.from(connectorSpec), configList);

    k8Deployment.create(se, specWithConfig, ce.getId());

    return ce;
  }

  private ConnectorEntity updateConnector(
      StreamEntity se,
      ConnectorSpec connectorSpec,
      ConnectorEntity ce,
      List<ConfigEntity> configList) {
    deleteK8Deployment(ce.getId());
    return createConnector(se, connectorSpec, ce, configList);
  }

  private UUID getStreamUUIDFromMap(Map<String, Object> map) {
    try {
      return UUID.fromString(map.get(StaticConfig.STREAM_NODE_TEXT).toString());
    } catch (Exception e) {
      throw new CreateConnectorException(StaticConfig.LoggerMessages.STREAM_NOT_FOUND);
    }
  }
}

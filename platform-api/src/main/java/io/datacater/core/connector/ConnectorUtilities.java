package io.datacater.core.connector;

import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.ConnectorNotFoundException;
import io.datacater.core.stream.StreamEntity;
import io.datacater.core.utilities.LoggerUtilities;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ContainerResource;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ConnectorUtilities {
  static final Logger LOGGER = Logger.getLogger(ConnectorUtilities.class);

  @Inject KubernetesClient client;

  public List<String> getConnectorLogsAsList(UUID connectorId, int tailingLines) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return Arrays.asList(k8Deployment.getLogs(connectorId, tailingLines).split("\n"));
  }

  public ContainerResource watchConnectorLogs(UUID connectorId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.watchLogs(connectorId);
  }

  public void deleteK8Deployment(UUID connectorId) {
    try {
      K8Deployment k8Deployment = new K8Deployment(client);
      k8Deployment.delete(connectorId);
    } catch (ConnectorNotFoundException e) {
      LoggerUtilities.logExceptionMessage(
          LOGGER,
          new Throwable().getStackTrace()[0].getMethodName(),
          String.format("Could not find Kubernetes deployment with id %s", connectorId.toString()));
    }
  }

  public ConnectorEntity createK8Deployment(
      StreamEntity streamEntity,
      ConnectorSpec connectorSpec,
      ConnectorEntity connectorEntity,
      List<ConfigEntity> configList) {
    K8Deployment k8Deployment = new K8Deployment(client);
    ConnectorSpec specWithConfig =
        ConfigUtilities.applyConfigsToConnector(ConnectorSpec.from(connectorSpec), configList);

    k8Deployment.create(streamEntity, specWithConfig, connectorEntity.getId());

    return connectorEntity;
  }

  public void updateK8Deployment(
      StreamEntity streamEntity,
      ConnectorSpec connectorSpec,
      ConnectorEntity connectorEntity,
      List<ConfigEntity> configList) {
    deleteK8Deployment(connectorEntity.getId());
    createK8Deployment(streamEntity, connectorSpec, connectorEntity, configList);
  }

  public void watchLogsRunner(UUID connectorId, @Context Sse sse, @Context SseEventSink eventSink)
      throws IOException {
    LogWatch lw = watchConnectorLogs(connectorId).watchLog();
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

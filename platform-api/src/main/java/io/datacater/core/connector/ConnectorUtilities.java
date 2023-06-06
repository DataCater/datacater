package io.datacater.core.connector;

import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.ConnectorNotFoundException;
import io.datacater.core.exceptions.DeploymentNotFoundException;
import io.datacater.core.stream.StreamEntity;
import io.datacater.core.utilities.LoggerUtilities;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ContainerResource;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
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

  public HttpRequest buildConnectorServiceRequest(UUID deploymentId) {
    K8Deployment k8Deployment = new K8Deployment(client);
    String ip = k8Deployment.getConnectorPodIp(deploymentId).replace(".", "-");
    String namespace = StaticConfig.EnvironmentVariables.NAMESPACE;
    int port = StaticConfig.EnvironmentVariables.CONCON_SIDECAR_HTTP_PORT;
    String protocol = StaticConfig.EnvironmentVariables.CONCON_SIDECAR_HTTP_PROTOCOL;
    String path = StaticConfig.EnvironmentVariables.CONNECTOR_HEALTH_PATH;

    String uriReady =
        String.format("%s://%s.%s.pod.cluster.local:%d%s", protocol, ip, namespace, port, path);

    return HttpRequest.newBuilder()
        .GET()
        .version(HttpClient.Version.HTTP_1_1)
        .uri(URI.create(uriReady))
        .build();
  }

  public Deployment getDeploymentObject(UUID connectorId) throws KubernetesClientException {
    try {
      List<Deployment> matchingDeployments =
          client
              .apps()
              .deployments()
              .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
              .withLabel(StaticConfig.UUID_TEXT, connectorId.toString())
              .list()
              .getItems();

      if (matchingDeployments.isEmpty()) {
        throw new DeploymentNotFoundException(StaticConfig.LoggerMessages.K8_DEPLOYMENT_NOT_FOUND);
      }

      return matchingDeployments.get(0);
    } catch (KubernetesClientException e) {
      throw new DeploymentNotFoundException(e.getMessage());
    }
  }
}

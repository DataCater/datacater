package io.datacater.core.deployment;

import io.datacater.core.config.ConfigEntity;
import io.datacater.core.config.ConfigUtilities;
import io.datacater.core.exceptions.DeploymentNotFoundException;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.Stream;
import io.datacater.core.utilities.LoggerUtilities;
import io.fabric8.kubernetes.client.KubernetesClient;
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
public class DeploymentUtilities {
  static final Logger LOGGER = Logger.getLogger(DeploymentUtilities.class);

  @Inject KubernetesClient client;

  public List<String> getDeploymentLogsAsList(UUID deploymentId, int replica, int tailingLines) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return Arrays.asList(k8Deployment.getLogs(deploymentId, replica, tailingLines).split("\n"));
  }

  public HttpRequest buildDeploymentServiceRequest(UUID deploymentId, String path, int replica) {
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

  public ContainerResource watchDeploymentLogs(UUID deploymentId, int replica) {
    K8Deployment k8Deployment = new K8Deployment(client);
    return k8Deployment.watchLogs(deploymentId, replica);
  }

  public void deleteK8Deployment(UUID deploymentId) {
    try {
      K8Deployment k8Deployment = new K8Deployment(client);
      k8Deployment.delete(deploymentId);
    } catch (DeploymentNotFoundException e) {
      LoggerUtilities.logExceptionMessage(
          LOGGER,
          new Throwable().getStackTrace()[0].getMethodName(),
          String.format(
              "Could not find Kubernetes deployment with id %s", deploymentId.toString()));
    }
  }

  public DeploymentEntity createDeployment(
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

  public void updateDeployment(
      PipelineEntity pe,
      Stream streamOut,
      Stream streamIn,
      DeploymentSpec deploymentSpec,
      DeploymentEntity de,
      List<ConfigEntity> configList) {
    deleteK8Deployment(de.getId());
    createDeployment(pe, streamOut, streamIn, deploymentSpec, de, configList);
  }

  public void watchLogsRunner(
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

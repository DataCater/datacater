package io.datacater.core.deployment;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class K8Service {
  private final KubernetesClient client;

  K8Service(KubernetesClient client) {
    this.client = client;
  }

  private boolean exists(String name) {
    return !getListByLabel(name).isEmpty();
  }

  public void delete(String name) {
    client.services().inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE).delete(get(name));
  }

  private Service get(String name) {
    return getListByLabel(name).get(0);
  }

  private List<Service> getListByLabel(String name) {
    return client
        .services()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withLabel(StaticConfig.DEPLOYMENT_SERVICE_TEXT, name)
        .list()
        .getItems();
  }

  public void create(String name) {
    if (!exists(name)) {
      createService(name);
    }
  }

  private Service createService(String name) {
    Service service =
        new ServiceBuilder()
            .withNewMetadata()
            .withName(name)
            .withLabels(Map.of(StaticConfig.DEPLOYMENT_SERVICE_TEXT, name))
            .endMetadata()
            .withNewSpec()
            .withSelector(Map.of(StaticConfig.DEPLOYMENT_SERVICE_TEXT, name))
            .withPorts(port())
            .endSpec()
            .build();
    client
        .services()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .createOrReplace(service);
    return service;
  }

  private ServicePort port() {
    return new ServicePortBuilder()
        .withProtocol(StaticConfig.TCP_TAG)
        .withPort(StaticConfig.EnvironmentVariables.DEPLOYMENT_CONTAINER_PORT)
        .build();
  }
}

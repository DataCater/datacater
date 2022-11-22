package io.datacater.core.deployment;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class K8Service {
  private final KubernetesClient client;

  K8Service(KubernetesClient client) {
    this.client = client;
  }

  private boolean exists(String name) {
    return getResource(name) != null;
  }

  private Service get(String name) {
    return getResource(name).get();
  }

  public void delete(String name) {
    getResource(name).delete();
  }

  private ServiceResource<Service> getResource(String name) {
    return client
        .services()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withName(name);
  }

  public Service getOrCreate(String name) {
    if (exists(name)) {
      return get(name);
    }
    return create(name);
  }

  private Service create(String name) {
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withSelector(Map.of(StaticConfig.DEPLOYMENT_SERVICE_TEXT, name))
        .withPorts(port())
        .endSpec()
        .build();
  }

  private ServicePort port() {
    return new ServicePortBuilder()
        .withProtocol("TCP")
        .withNewTargetPort(StaticConfig.EnvironmentVariables.DEPLOYMENT_CONTAINER_PORT)
        .build();
  }
}

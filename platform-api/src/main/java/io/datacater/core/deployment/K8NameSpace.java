package io.datacater.core.deployment;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class K8NameSpace {
  private final KubernetesClient client;

  K8NameSpace(KubernetesClient client) {
    this.client = client;
  }

  private boolean exists() {
    try {
      return client.namespaces().list().getItems().stream()
          .anyMatch(
              ns ->
                  ns.getMetadata()
                      .getNamespace()
                      .equals(StaticConfig.EnvironmentVariables.NAMESPACE));
    } catch (Exception e) {
      return false;
    }
  }

  protected void create() {
    if (!exists()) {
      Namespace ns =
          new NamespaceBuilder()
              .withNewMetadata()
              .withName(StaticConfig.EnvironmentVariables.NAMESPACE)
              .endMetadata()
              .build();
      client.namespaces().createOrReplace(ns);
    }
  }
}

package io.datacater.core.deployment;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class K8NameSpace {
  private static final Logger LOGGER = Logger.getLogger(K8NameSpace.class);
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

  private void delete(String namespace) {
    boolean status = client.namespaces().withName(namespace).delete();
    if (status) {
      LOGGER.info(StaticConfig.LoggerMessages.NAMESPACE_DELETED + namespace);
    } else {
      LOGGER.info(StaticConfig.LoggerMessages.NAMESPACE_NOT_DELETED + namespace);
    }
  }
}

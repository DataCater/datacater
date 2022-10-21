package io.datacater.core.deployment;

import io.datacater.core.pipeline.PipelineEntity;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class K8ConfigMap {
  private static final Logger LOGGER = Logger.getLogger(K8ConfigMap.class);
  private final KubernetesClient client;

  K8ConfigMap(KubernetesClient client) {
    this.client = client;
  }

  protected ConfigMap getOrCreate(String name, PipelineEntity pe) {
    if (exists(name)) {
      return get(name);
    }
    return create(name, pe);
  }

  private boolean exists(String name) {
    return client
        .configMaps()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .list()
        .getItems()
        .stream()
        .anyMatch(cm -> cm.getMetadata().getName().equals(name));
  }

  private ConfigMap get(String name) {
    return client
        .configMaps()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withName(name)
        .get();
  }

  private ConfigMap create(String name, PipelineEntity pe) {
    ConfigMap map =
        new ConfigMapBuilder()
            .withMetadata(getMetaData(name))
            .addToData(StaticConfig.SPEC, pe.getSpec().toString())
            .build();
    return client.configMaps().inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE).create(map);
  }

  private ObjectMeta getMetaData(String name) {
    return new ObjectMetaBuilder()
        .withName(name)
        .withNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .build();
  }
}

package io.datacater.core.kubernetes;

import io.datacater.core.exceptions.DatacaterException;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSetSpec;
import io.fabric8.kubernetes.api.model.apps.StatefulSetSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.vertx.core.shareddata.Shareable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jboss.logging.Logger;

@Singleton
public class LabeledStatefulSet implements Shareable {

  @Inject KubernetesClient client;

  static final Logger LOGGER = Logger.getLogger(LabeledStatefulSet.class);

  void createStatefulSet(StatefulSet statefulSet) {
    try {
      createNamespace();
      String message =
          String.format(
              "Executing StatefulSet Request against context := %s",
              client.getConfiguration().getCurrentContext());
      LOGGER.info(message);
      LOGGER.info("Creating StatefulSet.");
      client
          .apps()
          .statefulSets()
          .inNamespace(DataCaterK8sConfig.NAMESPACE)
          .createOrReplace(statefulSet);
    } catch (KubernetesClientException e) {
      String message = StringUtilities.wrapString(e.getMessage());
      throw new DatacaterException(message);
    }
  }

  private boolean namespaceExists() {
    try {
      return client.namespaces().list().getItems().stream()
          .anyMatch(ns -> ns.getMetadata().getNamespace().equals(DataCaterK8sConfig.NAMESPACE));
    } catch (Exception e) {
      return false;
    }
  }

  protected void createNamespace() {
    if (!namespaceExists()) {
      Namespace ns =
          new NamespaceBuilder()
              .withNewMetadata()
              .withName(DataCaterK8sConfig.NAMESPACE)
              .endMetadata()
              .build();
      client.namespaces().createOrReplace(ns);
    }
  }

  void createService() {
    Service service = buildService();
    String message =
        String.format("Creating service with name %s .", service.getMetadata().getName());
    LOGGER.info(message);

    client.services().inNamespace(DataCaterK8sConfig.NAMESPACE).createOrReplace(buildService());
  }

  void setClient(KubernetesClient client) {
    this.client = client;
  }

  StatefulSet blueprint() {
    return new StatefulSetBuilder(true)
        .withMetadata(this.datacaterMetadata())
        .withSpec(this.statefulSetPoolSpec())
        .build();
  }

  private ObjectMeta datacaterMetadata() {
    return new ObjectMetaBuilder(true)
        .withName(DataCaterK8sConfig.NAME)
        .withLabels(DataCaterK8sConfig.LABELS)
        .build();
  }

  private StatefulSetSpec statefulSetPoolSpec() {
    return new StatefulSetSpecBuilder(true)
        .withServiceName(DataCaterK8sConfig.PYTHON_RUNNER_SERVICE_NAME)
        .withReplicas(DataCaterK8sConfig.PYTHON_RUNNER_REPLICAS)
        .withSelector(new LabelSelectorBuilder().withMatchLabels(DataCaterK8sConfig.LABELS).build())
        .withTemplate(this.pythonRunner())
        .build();
  }

  private PodTemplateSpec pythonRunner() {
    return new PodTemplateSpecBuilder(true)
        .withMetadata(this.datacaterMetadata())
        .withSpec(this.pythonRunnerPodSpec())
        .build();
  }

  private PodSpec pythonRunnerPodSpec() {
    PodSpecBuilder podSpecBuilder =
        new PodSpecBuilder(true).withContainers(this.pythonRunnerContainer());

    DataCaterK8sConfig.IMAGE_PULL_SECRET.ifPresent(
        secret -> podSpecBuilder.withImagePullSecrets(new LocalObjectReference(secret)));

    return podSpecBuilder.build();
  }

  private Container pythonRunnerContainer() {
    return new ContainerBuilder(true)
        .withName(DataCaterK8sConfig.NAME)
        .withImage(
            String.format("%s:%s", DataCaterK8sConfig.IMAGE_NAME, DataCaterK8sConfig.IMAGE_TAG))
        .withPorts(this.containerPort())
        .withReadinessProbe(ready())
        .withLivenessProbe(ready())
        .build();
  }

  private Probe ready() {
    var get =
        new HTTPGetAction(
            null, // leave host empty on purpose, let K8s figure out pod local network.
            null, // no headers required
            "/health",
            new IntOrString(this.containerPort().getContainerPort()),
            "HTTP");

    return new ProbeBuilder()
        .withHttpGet(get)
        .withInitialDelaySeconds(0)
        .withPeriodSeconds(1)
        .withSuccessThreshold(1)
        .withFailureThreshold(10)
        .build();
  }

  private ContainerPort containerPort() {
    return new ContainerPortBuilder(true)
        .withContainerPort(DataCaterK8sConfig.CONTAINER_PORT)
        .withName("http")
        .build();
  }

  private Service buildService() {
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(DataCaterK8sConfig.PYTHON_RUNNER_SERVICE_NAME)
        .withLabels(DataCaterK8sConfig.LABELS)
        .endMetadata()
        .withNewSpec()
        .withClusterIP("None")
        .withPorts(
            new ServicePortBuilder()
                .withPort(DataCaterK8sConfig.CONTAINER_PORT)
                .withProtocol("TCP")
                .withName(DataCaterK8sConfig.CONTAINER_PORT_NAME)
                .withTargetPort(new IntOrString(DataCaterK8sConfig.CONTAINER_PORT))
                .build())
        .withSelector(DataCaterK8sConfig.LABELS)
        .endSpec()
        .build();
  }
}

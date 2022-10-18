package io.datacater.core.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

@Singleton
public class KubernetesDeployment {
  private static final Logger LOGGER = Logger.getLogger(KubernetesDeployment.class);

  static final String NAMESPACE =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.deployment.namespace", String.class)
          .orElse("datacater")
          .toLowerCase();
  static final String DEPLOYMENT_NAME =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.deployment.default-name", String.class)
          .orElse("datacater-" + UUID.randomUUID());
  static final String FULL_IMAGE_NAME =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.deployment.image", String.class)
          .orElse("ghcr.io/datacater/pipeline:1");

  final String PULL_POLICY = "IfNotPresent";
  final String PIPELINE_IN_CONFIG_NAME = "MP_MESSAGING_INCOMING_PIPELINE-IN_TOPIC";
  final String PIPELINE_OUT_CONFIG_NAME = "MP_MESSAGING_OUTGOING_PIPELINE-OUT_TOPIC";
  final String PIPELINE_IN = "pipeline-in";
  final String PIPELINE_OUT = "pipeline-out";
  final int REPLICAS = 1;

  final String DATACATER_PIPELINE = "datacater-pipeline";
  final String APP = "datacater.io/app";
  final String PIPELINE = "datacater.io/pipeline";
  final String REVISION = "datacater.io/revision";
  final String PIPELINE_NO = "1";
  final String PIPELINE_REV = "1";
  final String MOUNT_PATH = "/usr/app";
  final Map<String, Quantity> RESOURCE_REQUESTS =
      Map.of("cpu", new Quantity("0.1"), "memory", new Quantity("1.5Gi"));
  final Map<String, Quantity> RESOURCE_LIMITS = Map.of("memory", new Quantity("4Gi"));

  KubernetesClient client;

  public KubernetesDeployment(KubernetesClient client) {
    this.client = client;
  }

  public String createDeployment(
      DatacaterDeployment dcDeployment,
      PipelineEntity pe,
      StreamEntity streamIn,
      StreamEntity streamOut) {
    String name = getDeploymentName(dcDeployment.name());
    final String volumeName = name + "-volume";
    createNamespace();

    if (!pe.equals(new PipelineEntity())) {
      getConfigMap(name, pe);
    }

    if (deploymentExists(name)) {
      return name;
    }

    List<EnvVar> variables = getEnvironmentVariables(dcDeployment, streamIn, streamOut);

    var deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName(name)
            .addToLabels(
                Map.of(APP, DATACATER_PIPELINE, PIPELINE, PIPELINE_NO, REVISION, PIPELINE_REV))
            .endMetadata()
            .withNewSpec()
            .withReplicas(REPLICAS)
            .withMinReadySeconds(2)
            .withNewSelector()
            .addToMatchLabels(
                Map.of(APP, DATACATER_PIPELINE, PIPELINE, PIPELINE_NO, REVISION, PIPELINE_REV))
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .addToLabels(
                Map.of(APP, DATACATER_PIPELINE, PIPELINE, PIPELINE_NO, REVISION, PIPELINE_REV))
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName(name)
            .withImage(FULL_IMAGE_NAME)
            .withImagePullPolicy(PULL_POLICY)
            .withEnv(variables)
            .withNewResources()
            .withRequests(RESOURCE_REQUESTS)
            .withLimits(RESOURCE_LIMITS)
            .endResources()
            .withVolumeMounts(
                new VolumeMountBuilder().withName(volumeName).withMountPath(MOUNT_PATH).build())
            .endContainer()
            .withVolumes(
                new VolumeBuilder()
                    .withName(volumeName)
                    .withConfigMap(new ConfigMapVolumeSourceBuilder().withName(name).build())
                    .build())
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    // need to add imagepullsecret
    // need to attach stream in and stream out bootstrap.servers to env
    client.apps().deployments().inNamespace(NAMESPACE).create(deployment);
    return name;
  }

  public String getDeploymentLogs(String name) {
    return client.apps().deployments().inNamespace(NAMESPACE).withName(name).getLog(true);
  }

  public void deleteDeployment(String deploymentName) {
    Boolean status =
        client.apps().deployments().inNamespace(NAMESPACE).withName(deploymentName).delete();
    if (status) {
      LOGGER.info("DatacaterDeployment deleted successfully: " + deploymentName);
    } else {
      LOGGER.info("DatacaterDeployment could not be deleted: " + deploymentName);
    }
  }

  public DeploymentStatus getStats(String deploymentName) {
    return client
        .apps()
        .deployments()
        .inNamespace(NAMESPACE)
        .withName(deploymentName)
        .get()
        .getStatus();
  }

  private boolean namespaceExists() {
    try {
      return client.namespaces().list().getItems().stream()
          .anyMatch(ns -> ns.getMetadata().getNamespace().equals(NAMESPACE));
    } catch (Exception e) {
      return false;
    }
  }

  private boolean deploymentExists(String name) {
    return client.apps().deployments().inNamespace(NAMESPACE).withName(name).get() != null;
  }

  private void createNamespace() {
    if (!namespaceExists()) {
      Namespace ns =
          new NamespaceBuilder().withNewMetadata().withName(NAMESPACE).endMetadata().build();
      client.namespaces().createOrReplace(ns);
    }
  }

  private void deleteNamespace(String namespace) {
    boolean status = client.namespaces().withName(namespace).delete();
    if (status) {
      LOGGER.info("Deleting namespace with name: " + namespace);
    } else {
      LOGGER.info("Couldn't delete namespace with name: " + namespace);
    }
  }

  private String getDeploymentName(String name) {
    if (name == null || name.isEmpty()) {
      return DEPLOYMENT_NAME;
    }
    return name;
  }

  private String getPipelineSpecItem(String item, JsonNode node) {
    if (node != null && !node.findValue(item).asText().isEmpty()) {
      return node.findValue(item).asText();
    }
    return item;
  }

  private boolean configMapExists(String name) {
    return client.configMaps().inNamespace(NAMESPACE).list().getItems().stream()
        .anyMatch(cm -> cm.getMetadata().getName().equals(name));
  }

  private ConfigMap getConfigMap(String name, PipelineEntity pe) {
    if (!configMapExists(name)) {
      return createConfigMap(name, pe);
    }
    return client.configMaps().inNamespace(NAMESPACE).withName(name).get();
  }

  private ConfigMap createConfigMap(String name, PipelineEntity pe) {
    ConfigMap map =
        new ConfigMapBuilder()
            .withMetadata(getMetaData(name))
            .addToData("spec", pe.getSpec().toString())
            .build();
    return client.configMaps().inNamespace(NAMESPACE).create(map);
  }

  private ObjectMeta getMetaData(String name) {
    return new ObjectMetaBuilder().withName(name).withNamespace(NAMESPACE).build();
  }

  private List<EnvVar> getEnvironmentVariables(
      DatacaterDeployment dcDeployment, StreamEntity streamIn, StreamEntity streamOut) {

    List<EnvVar> variables = new ArrayList<>();

    variables.add(
        new EnvVarBuilder()
            .withName(PIPELINE_OUT_CONFIG_NAME)
            .withValue(dcDeployment.spec().getPipelineOut())
            .build());

    variables.add(
        new EnvVarBuilder()
            .withName(PIPELINE_IN_CONFIG_NAME)
            .withValue(dcDeployment.spec().getPipelineIn())
            .build());

    String streamInBootsrapServers = getFromNode(streamIn.getSpec(), "bootstrap.servers");
    String streamInKeyDeserializer = getFromNode(streamIn.getSpec(), "key.deserializer");
    String streamInValueDeserializer = getFromNode(streamIn.getSpec(), "value.deserializer");

    String streamOutBootsrapServers = getFromNode(streamOut.getSpec(), "bootstrap.servers");
    String streamOutKeySerializer = getFromNode(streamOut.getSpec(), "key.serializer");
    String streamOutValueSerializer = getFromNode(streamOut.getSpec(), "value.serializer");

    variables.add(
        new EnvVarBuilder()
            .withName("MP_MESSAGING_INCOMING_PIPELINE-IN_BOOTSTRAP_SERVERS")
            .withValue(streamInBootsrapServers)
            .build());
    variables.add(
        new EnvVarBuilder()
            .withName("MP_MESSAGING_INCOMING_PIPELINE-IN_KEY_DESERIALIZER")
            .withValue(streamInKeyDeserializer)
            .build());
    variables.add(
        new EnvVarBuilder()
            .withName("MP_MESSAGING_INCOMING_PIPELINE-IN_VALUE_DESERIALIZER")
            .withValue(streamInValueDeserializer)
            .build());

    variables.add(
        new EnvVarBuilder()
            .withName("MP_MESSAGING_OUTGOING_PIPELINE-OUT_BOOTSTRAP_SERVERS")
            .withValue(streamOutBootsrapServers)
            .build());
    variables.add(
        new EnvVarBuilder()
            .withName("MP_MESSAGING_OUTGOING_PIPELINE-OUT_KEY_SERIALIZER")
            .withValue(streamOutKeySerializer)
            .build());
    variables.add(
        new EnvVarBuilder()
            .withName("MP_MESSAGING_OUTGOING_PIPELINE-OUT_VALUE_SERIALIZER")
            .withValue(streamOutValueSerializer)
            .build());

    return variables;
  }

  private String getFromNode(JsonNode node, String field) {
    if (node.get(field) != null) {
      return node.get(field).toString();
    }
    if (field.contains("deserializer")) {
      return io.datacater.core.serde.JsonDeserializer.class.toString();
    }
    if (field.contains("serializer")) {
      return io.datacater.core.serde.JsonSerializer.class.toString();
    }
    if (field.contains("bootstrap.servers")) {
      return "localhost:9092";
    }
    return "";
  }
}

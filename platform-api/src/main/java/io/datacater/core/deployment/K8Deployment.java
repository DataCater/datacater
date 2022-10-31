package io.datacater.core.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Singleton;
import org.jboss.logging.Logger;

@Singleton
public class K8Deployment {
  private static final Logger LOGGER = Logger.getLogger(K8Deployment.class);
  private final KubernetesClient client;
  private final K8ConfigMap k8ConfigMap;
  private final K8NameSpace k8NameSpace;

  public K8Deployment(KubernetesClient client) {
    this.client = client;
    this.k8NameSpace = new K8NameSpace(client);
    this.k8ConfigMap = new K8ConfigMap(client);
  }

  public UUID create(PipelineEntity pe, StreamEntity streamIn, StreamEntity streamOut) {
    final String name = StaticConfig.DEPLOYMENT_NAME_PREFIX + UUID.randomUUID();
    final String volumeName = name + StaticConfig.VOLUME_NAME_SUFFIX;
    k8NameSpace.create();

    if (!pe.equals(new PipelineEntity())) {
      k8ConfigMap.getOrCreate(name, pe);
    }
    UUID deploymentId = UUID.randomUUID();

    List<EnvVar> variables = getEnvironmentVariables(streamIn, streamOut);

    Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName(name)
            .addToLabels(
                Map.of(
                    StaticConfig.APP,
                    StaticConfig.DATACATER_PIPELINE,
                    StaticConfig.PIPELINE,
                    StaticConfig.PIPELINE_NO,
                    StaticConfig.REVISION,
                    StaticConfig.PIPELINE_REV,
                    StaticConfig.UUID_TEXT,
                    deploymentId.toString()))
            .endMetadata()
            .withNewSpec()
            .withReplicas(StaticConfig.REPLICAS)
            .withMinReadySeconds(2)
            .withNewSelector()
            .addToMatchLabels(
                Map.of(
                    StaticConfig.APP,
                    StaticConfig.DATACATER_PIPELINE,
                    StaticConfig.PIPELINE,
                    StaticConfig.PIPELINE_NO,
                    StaticConfig.REVISION,
                    StaticConfig.PIPELINE_REV,
                    StaticConfig.UUID_TEXT,
                    deploymentId.toString()))
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .addToLabels(
                Map.of(
                    StaticConfig.APP,
                    StaticConfig.DATACATER_PIPELINE,
                    StaticConfig.PIPELINE,
                    StaticConfig.PIPELINE_NO,
                    StaticConfig.REVISION,
                    StaticConfig.PIPELINE_REV,
                    StaticConfig.UUID_TEXT,
                    deploymentId.toString()))
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName(name)
            .withImage(StaticConfig.EnvironmentVariables.FULL_IMAGE_NAME)
            .withImagePullPolicy(StaticConfig.PULL_POLICY)
            .withEnv(variables)
            .withNewResources()
            .withRequests(StaticConfig.RESOURCE_REQUESTS)
            .withLimits(StaticConfig.RESOURCE_LIMITS)
            .endResources()
            .withVolumeMounts(
                new VolumeMountBuilder()
                    .withName(volumeName)
                    .withMountPath(StaticConfig.MOUNT_PATH)
                    .build())
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

    client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .create(deployment);
    return deploymentId;
  }

  public String getLogs(UUID deploymentId) {
    return client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withName(getDeploymentName(deploymentId))
        .getLog(true);
  }

  public RollableScalableResource<Deployment> watchLogs(UUID deploymentId) {
    return client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withName(getDeploymentName(deploymentId));
  }

  public void delete(UUID deploymentId) {
    String name = getDeploymentName(deploymentId);
    Boolean status =
        client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withName(name)
            .delete();
    if (status) {
      LOGGER.info(StaticConfig.LoggerMessages.DEPLOYMENT_DELETED + name);
    } else {
      LOGGER.info(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_DELETED + name);
    }
  }

  public DeploymentStatus getDeploymentStatus(String deploymentName) {
    return client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withName(deploymentName)
        .get()
        .getStatus();
  }

  public ListMeta getDeployments() {
    return client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .list()
        .getMetadata();
  }

  public ObjectMeta getDeployment(UUID deploymentId) {

    return client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withName(getDeploymentName(deploymentId))
        .get()
        .getMetadata();
  }

  private boolean exists(UUID deploymentId) {
    return !client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withLabel(StaticConfig.UUID_TEXT, deploymentId.toString())
        .list()
        .getItems()
        .isEmpty();
  }

  private String getDeploymentName(UUID deploymentId) {
    List<Deployment> deployments =
        client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withLabel(StaticConfig.UUID_TEXT, deploymentId.toString())
            .list()
            .getItems();

    if (deployments.isEmpty()) {
      return null;
    }
    return deployments.get(0).getMetadata().getName();
  }

  private List<EnvVar> getEnvironmentVariables(StreamEntity streamIn, StreamEntity streamOut) {

    String streamInBootstrapServers =
        getEnvVariableFromNode(streamIn.getSpec(), StaticConfig.BOOTSTRAP_SERVERS);
    String streamInKeyDeserializer =
        getEnvVariableFromNode(streamIn.getSpec(), StaticConfig.KEY_DESERIALIZER);
    String streamInValueDeserializer =
        getEnvVariableFromNode(streamIn.getSpec(), StaticConfig.VALUE_DESERIALIZER);
    String streamOutBootstrapServers =
        getEnvVariableFromNode(streamOut.getSpec(), StaticConfig.BOOTSTRAP_SERVERS);
    String streamOutKeySerializer =
        getEnvVariableFromNode(streamOut.getSpec(), StaticConfig.KEY_SERIALIZER);
    String streamOutValueSerializer =
        getEnvVariableFromNode(streamOut.getSpec(), StaticConfig.VALUE_SERIALIZER);

    List<EnvVar> variables = new ArrayList<>();
    variables.add(createEnvVariable(StaticConfig.STREAM_OUT_CONFIG_NAME, streamIn.getName()));
    variables.add(createEnvVariable(StaticConfig.STREAM_IN_CONFIG_NAME, streamOut.getName()));
    variables.add(
        createEnvVariable(StaticConfig.STREAM_IN_BOOTSTRAP_SERVER, streamInBootstrapServers));
    variables.add(
        createEnvVariable(StaticConfig.STREAM_IN_KEY_DESERIALIZER, streamInKeyDeserializer));
    variables.add(
        createEnvVariable(StaticConfig.STREAM_IN_VALUE_DESERIALIZER, streamInValueDeserializer));
    variables.add(
        createEnvVariable(StaticConfig.STREAM_OUT_BOOTSTRAP_SERVER, streamOutBootstrapServers));
    variables.add(
        createEnvVariable(StaticConfig.STREAM_OUT_KEY_SERIALIZER, streamOutKeySerializer));
    variables.add(
        createEnvVariable(StaticConfig.STREAM_OUT_VALUE_SERIALIZER, streamOutValueSerializer));

    return variables;
  }

  private EnvVar createEnvVariable(String name, String value) {
    return new EnvVarBuilder().withName(name).withValue(value).build();
  }

  private String getEnvVariableFromNode(JsonNode node, String field) {
    if (node.get(field) != null) {
      return node.get(field).toString();
    }
    if (field.contains(StaticConfig.DESERIALIZER)) {
      return io.datacater.core.serde.JsonDeserializer.class.toString();
    }
    if (field.contains(StaticConfig.SERIALIZER)) {
      return io.datacater.core.serde.JsonSerializer.class.toString();
    }
    if (field.contains(StaticConfig.BOOTSTRAP_SERVERS)) {
      return StaticConfig.LOCALHOST_BOOTSTRAP_SERVER;
    }
    return StaticConfig.EMPTY_STRING;
  }
}

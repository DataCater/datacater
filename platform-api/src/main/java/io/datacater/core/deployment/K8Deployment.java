package io.datacater.core.deployment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.exceptions.CreateDeploymentException;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.util.*;
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

  public UUID create(
      PipelineEntity pe,
      StreamEntity streamIn,
      StreamEntity streamOut,
      DeploymentSpec deploymentSpec) {
    UUID deploymentId = UUID.randomUUID();
    final String name = StaticConfig.DEPLOYMENT_NAME_PREFIX + deploymentId;
    final String configmapName = StaticConfig.CONFIGMAP_NAME_PREFIX + deploymentId;
    final String volumeName = StaticConfig.VOLUME_NAME_PREFIX + deploymentId;
    k8NameSpace.create();
    k8ConfigMap.getOrCreate(configmapName, pe);

    List<EnvVar> variables = getEnvironmentVariables(streamIn, streamOut, deploymentSpec);

    Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName(name)
            .addToLabels(getLabels(deploymentId))
            .endMetadata()
            .withNewSpec()
            .withReplicas(StaticConfig.EnvironmentVariables.REPLICAS)
            .withMinReadySeconds(StaticConfig.EnvironmentVariables.READY_SECONDS)
            .withNewSelector()
            .addToMatchLabels(getLabels(deploymentId))
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .addToLabels(getLabels(deploymentId))
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName(name)
            .withImage(StaticConfig.EnvironmentVariables.FULL_IMAGE_NAME)
            .withImagePullPolicy(StaticConfig.EnvironmentVariables.PULL_POLICY)
            .withEnv(variables)
            .withNewResources()
            .withRequests(StaticConfig.RESOURCE_REQUESTS)
            .withLimits(StaticConfig.RESOURCE_LIMITS)
            .endResources()
            .withVolumeMounts(getVolumeMount(volumeName))
            .endContainer()
            .withVolumes(getVolume(volumeName, configmapName))
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .create(deployment);

    if (!exists(deploymentId)) {
      throw new CreateDeploymentException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_CREATED);
    }
    return deploymentId;
  }

  private static Map<String, String> getLabels(UUID deploymentId) {
    return Map.of(
        StaticConfig.APP,
        StaticConfig.DATACATER_PIPELINE,
        StaticConfig.PIPELINE,
        StaticConfig.PIPELINE_NO,
        StaticConfig.REVISION,
        StaticConfig.PIPELINE_REV,
        StaticConfig.UUID_TEXT,
        deploymentId.toString());
  }

  private static VolumeMount getVolumeMount(String volumeName) {
    return new VolumeMountBuilder()
        .withName(volumeName)
        .withMountPath(StaticConfig.MOUNT_PATH)
        .build();
  }

  private static Volume getVolume(String volumeName, String deploymentName) {
    return new VolumeBuilder()
        .withName(volumeName)
        .withConfigMap(configMapVolumeSource(deploymentName))
        .build();
  }

  private static ConfigMapVolumeSource configMapVolumeSource(String deploymentName) {
    return new ConfigMapVolumeSourceBuilder().withName(deploymentName).build();
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
    if (Boolean.TRUE.equals(status)) {
      LOGGER.info(StaticConfig.LoggerMessages.DEPLOYMENT_DELETED + name);
    } else {
      LOGGER.info(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_DELETED + name);
    }
  }

  public DeploymentSpec getDeployments() {
    return deploymentListToDeploymentSpec(
        client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withLabel(StaticConfig.APP, StaticConfig.DATACATER_PIPELINE)
            .list()
            .getItems());
  }

  private DeploymentSpec deploymentListToDeploymentSpec(List<Deployment> deployments) {
    Map<String, Object> map = new HashMap<>();
    for (Deployment deployment : deployments) {
      map.putAll(objectMetaToMap(deployment.getMetadata()));
    }
    return new DeploymentSpec(map);
  }

  private Map<String, Object> objectMetaToMap(ObjectMeta metaData) {
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> node = new HashMap<>();
    if (metaData.getAdditionalProperties() != null) {
      node.putAll(metaData.getAdditionalProperties());
    }
    if (metaData.getLabels() != null) {
      node.putAll(metaData.getLabels());
    }
    if (metaData.getAnnotations() != null) {
      node.putAll(metaData.getAnnotations());
    }
    map.put(metaData.getName(), node);
    return map;
  }

  public DeploymentSpec getDeployment(UUID deploymentId) {

    return new DeploymentSpec(
        objectMetaToMap(
            client
                .apps()
                .deployments()
                .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                .withName(getDeploymentName(deploymentId))
                .get()
                .getMetadata()));
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

  private List<EnvVar> getEnvironmentVariables(
      StreamEntity streamIn, StreamEntity streamOut, DeploymentSpec deploymentSpec) {
    Map<String, Object> streamInConfig = getNode(StaticConfig.STREAMIN_CONFIG_TEXT, deploymentSpec);
    Map<String, Object> streamOutConfig =
        getNode(StaticConfig.STREAMOUT_CONFIG_TEXT, deploymentSpec);

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

    streamInConfig.putIfAbsent(StaticConfig.BOOTSTRAP_SERVERS, streamInBootstrapServers);
    streamInConfig.putIfAbsent(StaticConfig.KEY_DESERIALIZER, streamInKeyDeserializer);
    streamInConfig.putIfAbsent(StaticConfig.VALUE_DESERIALIZER, streamInValueDeserializer);

    streamOutConfig.putIfAbsent(StaticConfig.BOOTSTRAP_SERVERS, streamOutBootstrapServers);
    streamOutConfig.putIfAbsent(StaticConfig.KEY_SERIALIZER, streamOutKeySerializer);
    streamOutConfig.putIfAbsent(StaticConfig.VALUE_SERIALIZER, streamOutValueSerializer);

    variables.add(
        createEnvVariable(StaticConfig.DC_STREAMIN_CONFIG_TEXT, streamInConfig.toString()));
    variables.add(
        createEnvVariable(StaticConfig.DC_STREAMOUT_CONFIG_TEXT, streamOutConfig.toString()));
    return variables;
  }

  private Map<String, Object> getNode(String node, DeploymentSpec spec) {
    ObjectMapper om = new ObjectMapper();
    Map<String, Object> config =
        om.convertValue(spec.deployment().get(node), new TypeReference<>() {});
    if (config == null) {
      return new HashMap<>();
    }
    return config;
  }

  private EnvVar createEnvVariable(String name, String value) {
    return new EnvVarBuilder().withName(name).withValue(value).build();
  }

  private String getEnvVariableFromNode(JsonNode node, String field) {
    if (node.get(field) != null) {
      return node.get(field).toString();
    }
    if (field.contains(StaticConfig.DESERIALIZER)) {
      return io.datacater.core.serde.JsonDeserializer.class.getName();
    }
    if (field.contains(StaticConfig.SERIALIZER)) {
      return io.datacater.core.serde.JsonSerializer.class.getName();
    }
    if (field.contains(StaticConfig.BOOTSTRAP_SERVERS)) {
      return StaticConfig.LOCALHOST_BOOTSTRAP_SERVER;
    }
    return StaticConfig.EMPTY_STRING;
  }
}

package io.datacater.core.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.exceptions.CreateDeploymentException;
import io.datacater.core.exceptions.DeploymentNotFoundException;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.util.*;
import javax.inject.Singleton;
import org.jboss.logging.Logger;

@Singleton
public class K8Deployment {
  private static final Logger LOGGER = Logger.getLogger(K8Deployment.class);
  private final KubernetesClient client;
  private final K8ConfigMap k8ConfigMap;
  private final K8Service k8Service;

  public K8Deployment(KubernetesClient client) {
    this.client = client;
    this.k8ConfigMap = new K8ConfigMap(client);
    this.k8Service = new K8Service(client);
  }

  public Map<String, Object> create(
      PipelineEntity pe,
      StreamEntity streamIn,
      StreamEntity streamOut,
      DeploymentSpec deploymentSpec,
      UUID deploymentId) {
    final String name = StaticConfig.DEPLOYMENT_NAME_PREFIX + deploymentId;
    final String configmapName = StaticConfig.CONFIGMAP_NAME_PREFIX + deploymentId;
    final String configmapVolumeName = StaticConfig.CONFIGMAP_VOLUME_NAME_PREFIX + deploymentId;
    final String dataShareVolumeName = StaticConfig.DATA_SHARE_VOLUME_NAME_PREFIX + deploymentId;
    final String serviceName = StaticConfig.SERVICE_NAME_PREFIX + deploymentId;

    List<EnvVar> variables =
        getEnvironmentVariables(streamIn, streamOut, deploymentSpec, deploymentId);

    try {
      Deployment deployment =
          new DeploymentBuilder()
              .withNewMetadata()
              .withName(name)
              .addToLabels(getLabels(deploymentId, deploymentSpec.name(), serviceName))
              .endMetadata()
              .withNewSpec()
              .withReplicas(StaticConfig.EnvironmentVariables.REPLICAS)
              .withMinReadySeconds(StaticConfig.EnvironmentVariables.READY_SECONDS)
              .withNewSelector()
              .addToMatchLabels(getLabels(deploymentId, deploymentSpec.name(), serviceName))
              .endSelector()
              .withNewTemplate()
              .withNewMetadata()
              .addToLabels(getLabels(deploymentId, deploymentSpec.name(), serviceName))
              .endMetadata()
              .withNewSpec()
              .addAllToContainers(
                  List.of(
                      deploymentContainer(
                          name, configmapVolumeName, dataShareVolumeName, variables),
                      pythonRunnerContainer(configmapVolumeName, dataShareVolumeName)))
              .withVolumes(
                  getConfigMapVolume(configmapVolumeName, configmapName),
                  getDataShareVolume(dataShareVolumeName))
              .endSpec()
              .endTemplate()
              .endSpec()
              .build();

      client
          .apps()
          .deployments()
          .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
          .create(deployment);

    } catch (KubernetesClientException ex) {
      throw new CreateDeploymentException(StringUtilities.wrapString(ex.getMessage()));
    }

    if (!exists(deploymentId)) {
      throw new CreateDeploymentException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_CREATED);
    }
    k8ConfigMap.getOrCreate(configmapName, pe);
    k8Service.create(serviceName);
    return getDeployment(deploymentId);
  }

  private static Map<String, String> getLabels(
      UUID deploymentId, String prettyName, String serviceName) {
    return Map.of(
        StaticConfig.APP,
        StaticConfig.DATACATER_PIPELINE,
        StaticConfig.PIPELINE,
        StaticConfig.PIPELINE_NO,
        StaticConfig.REVISION,
        StaticConfig.PIPELINE_REV,
        StaticConfig.UUID_TEXT,
        deploymentId.toString(),
        StaticConfig.DEPLOYMENT_NAME_TEXT,
        prettyName,
        StaticConfig.DEPLOYMENT_SERVICE_TEXT,
        serviceName);
  }

  private Container pythonRunnerContainer(String configmapVolumeName, String dataShareVolumeName) {
    return new ContainerBuilder(true)
        .withName(StaticConfig.PYTHON_RUNNER_NAME)
        .withImage(
            String.format(
                "%s:%s",
                StaticConfig.EnvironmentVariables.PYTHON_RUNNER_IMAGE_NAME,
                StaticConfig.EnvironmentVariables.PYTHON_RUNNER_IMAGE_TAG))
        .withPorts(
            this.containerPort(StaticConfig.EnvironmentVariables.PYTHON_RUNNER_CONTAINER_PORT))
        .withVolumeMounts(
            getVolumeMount(configmapVolumeName, StaticConfig.CONFIGMAP_MOUNT_PATH),
            getVolumeMount(dataShareVolumeName, StaticConfig.DATA_SHARE_MOUNT_PATH))
        .build();
  }

  private Container deploymentContainer(
      String name, String configmapVolumeName, String dataShareVolumeName, List<EnvVar> variables) {
    return new ContainerBuilder(true)
        .withName(name)
        .withImage(StaticConfig.EnvironmentVariables.FULL_IMAGE_NAME)
        .withImagePullPolicy(StaticConfig.EnvironmentVariables.PULL_POLICY)
        .withEnv(variables)
        .withNewResources()
        .withRequests(StaticConfig.RESOURCE_REQUESTS)
        .withLimits(StaticConfig.RESOURCE_LIMITS)
        .endResources()
        .withPorts(this.containerPort(StaticConfig.EnvironmentVariables.DEPLOYMENT_CONTAINER_PORT))
        .withVolumeMounts(
            getVolumeMount(configmapVolumeName, StaticConfig.CONFIGMAP_MOUNT_PATH),
            getVolumeMount(dataShareVolumeName, StaticConfig.DATA_SHARE_MOUNT_PATH))
        .build();
  }

  private ContainerPort containerPort(int port) {
    return new ContainerPortBuilder(true)
        .withContainerPort(port)
        .withName(StaticConfig.HTTP)
        .build();
  }

  private static VolumeMount getVolumeMount(String volumeName, String mountPath) {
    return new VolumeMountBuilder().withName(volumeName).withMountPath(mountPath).build();
  }

  private static Volume getConfigMapVolume(String volumeName, String deploymentName) {
    return new VolumeBuilder()
        .withName(volumeName)
        .withConfigMap(configMapVolumeSource(deploymentName))
        .build();
  }

  private static Volume getDataShareVolume(String volumeName) {
    return new VolumeBuilder().withName(volumeName).build();
  }

  private static ConfigMapVolumeSource configMapVolumeSource(String deploymentName) {
    return new ConfigMapVolumeSourceBuilder().withName(deploymentName).build();
  }

  public String getClusterIp(UUID deploymentId) {
    final String serviceName = StaticConfig.SERVICE_NAME_PREFIX + deploymentId;
    return k8Service.getClusterIp(serviceName);
  }

  public String getLogs(UUID deploymentId) {
    return client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withName(getDeploymentName(deploymentId))
        .inContainer(StaticConfig.DEPLOYMENT_NAME_PREFIX + deploymentId)
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
    String serviceName = StaticConfig.SERVICE_NAME_PREFIX + deploymentId;
    String configMapName = StaticConfig.CONFIGMAP_NAME_PREFIX + deploymentId;
    Boolean status =
        client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withName(name)
            .delete();

    if (Boolean.TRUE.equals(status)) {
      k8Service.delete(serviceName);
      k8ConfigMap.delete(configMapName);
      LOGGER.info(String.format(StaticConfig.LoggerMessages.DEPLOYMENT_DELETED, name));
    } else {
      LOGGER.info(String.format(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_DELETED, name));
    }
  }

  private Map<String, Object> deploymentToMetaDataMap(Deployment deployment) {
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> node = new HashMap<>();
    if (deployment.getMetadata().getLabels() != null) {
      node.putAll(deployment.getMetadata().getLabels());
    }
    if (deployment.getMetadata().getAnnotations() != null) {
      node.putAll(deployment.getMetadata().getAnnotations());
    }
    if (deployment.getStatus() == null) {
      // return before trying to add Status MetaData
      map.put(deployment.getMetadata().getName(), node);
      return map;
    }

    if (deployment.getStatus().getAdditionalProperties() != null) {
      node.putAll(deployment.getStatus().getAdditionalProperties());
    }
    if (deployment.getStatus().getConditions() != null) {
      node.put(StaticConfig.CONDITIONS, deployment.getStatus().getConditions());
    }
    map.put(deployment.getMetadata().getName(), node);
    return map;
  }

  public Map<String, Object> getDeployment(UUID deploymentId) {
    try {
      return deploymentToMetaDataMap(
          client
              .apps()
              .deployments()
              .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
              .withName(getDeploymentName(deploymentId))
              .get());
    } catch (DeploymentNotFoundException ex) {
      Map<String, Object> errorMap = new HashMap<>();
      Map<String, Object> messageMap = new HashMap<>();
      messageMap.put(StaticConfig.MESSAGE_TAG, StaticConfig.LoggerMessages.K8_DEPLOYMENT_NOT_FOUND);
      errorMap.put(StaticConfig.ERROR_TAG, messageMap);
      return errorMap;
    }
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
      throw new DeploymentNotFoundException(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_FOUND);
    }
    return deployments.get(0).getMetadata().getName();
  }

  private List<EnvVar> getEnvironmentVariables(
      StreamEntity streamIn, StreamEntity streamOut, DeploymentSpec deploymentSpec, UUID uuid) {

    // Get Apache Kafka-related configs from stream in and stream out
    Map<String, Object> streamInConfig = nodeToMap(streamIn.getSpec().get(StaticConfig.KAFKA_TAG));
    Map<String, Object> streamOutConfig =
        nodeToMap(streamOut.getSpec().get(StaticConfig.KAFKA_TAG));

    // Let deployment overwrite config of stream in and stream out
    streamInConfig.putAll(getNode(StaticConfig.STREAMIN_CONFIG_TEXT, deploymentSpec));
    streamOutConfig.putAll(getNode(StaticConfig.STREAMOUT_CONFIG_TEXT, deploymentSpec));

    // Remove topic-related configs from stream in and stream out
    streamInConfig.remove(StaticConfig.TOPIC_TAG);
    streamOutConfig.remove(StaticConfig.TOPIC_TAG);

    // Initialize stream in with default values, if needed
    streamInConfig.putIfAbsent(
        StaticConfig.BOOTSTRAP_SERVERS,
        getEnvVariableFromNode(streamIn.getSpec(), StaticConfig.BOOTSTRAP_SERVERS));
    streamInConfig.putIfAbsent(
        StaticConfig.KEY_DESERIALIZER,
        getEnvVariableFromNode(streamIn.getSpec(), StaticConfig.KEY_DESERIALIZER));
    streamInConfig.putIfAbsent(
        StaticConfig.VALUE_DESERIALIZER,
        getEnvVariableFromNode(streamIn.getSpec(), StaticConfig.VALUE_DESERIALIZER));

    // Initialize stream out with default values, if needed
    streamOutConfig.putIfAbsent(
        StaticConfig.BOOTSTRAP_SERVERS,
        getEnvVariableFromNode(streamOut.getSpec(), StaticConfig.BOOTSTRAP_SERVERS));
    streamOutConfig.putIfAbsent(
        StaticConfig.KEY_SERIALIZER,
        getEnvVariableFromNode(streamOut.getSpec(), StaticConfig.KEY_SERIALIZER));
    streamOutConfig.putIfAbsent(
        StaticConfig.VALUE_SERIALIZER,
        getEnvVariableFromNode(streamOut.getSpec(), StaticConfig.VALUE_SERIALIZER));

    // Store Serde-related information
    Map<String, Object> dataCaterConfig =
        Map.of(
            StaticConfig.KEY_DESERIALIZER, streamInConfig.get(StaticConfig.KEY_DESERIALIZER),
            StaticConfig.VALUE_DESERIALIZER, streamInConfig.get(StaticConfig.VALUE_DESERIALIZER),
            StaticConfig.KEY_SERIALIZER, streamOutConfig.get(StaticConfig.KEY_SERIALIZER),
            StaticConfig.VALUE_SERIALIZER, streamOutConfig.get(StaticConfig.VALUE_SERIALIZER));

    // Remove Serde-related information from channel configs
    streamInConfig.remove(StaticConfig.KEY_DESERIALIZER);
    streamInConfig.remove(StaticConfig.VALUE_DESERIALIZER);
    streamOutConfig.remove(StaticConfig.KEY_SERIALIZER);
    streamOutConfig.remove(StaticConfig.VALUE_SERIALIZER);

    /**
     * Set `group.id` to the UUID of the deployment, if absent, such that deployments with more than
     * one replica can make use of Apache Kafka's consumer group protocol.
     */
    streamInConfig.putIfAbsent(StaticConfig.GROUP_ID, uuid);

    List<EnvVar> environmentVariables = new ArrayList<>();

    // Pass serde config as environment variables
    for (String configOption : dataCaterConfig.keySet()) {
      String configOptionAsEnvVariable = transformConfigOptionToEnvVariable(configOption);
      environmentVariables.add(
          createEnvVariable(
              StaticConfig.DATACATER_SERDE_ENV_PREFIX,
              configOptionAsEnvVariable,
              dataCaterConfig.get(configOption).toString()));
    }

    // Pass stream in config as environment variables
    for (String configOption : streamInConfig.keySet()) {
      String configOptionAsEnvVariable = transformConfigOptionToEnvVariable(configOption);
      environmentVariables.add(
          createEnvVariable(
              StaticConfig.STREAMIN_ENV_PREFIX,
              configOptionAsEnvVariable,
              streamInConfig.get(configOption).toString()));
    }

    // Pass stream out config as environment variables
    for (String configOption : streamOutConfig.keySet()) {
      String configOptionAsEnvVariable = transformConfigOptionToEnvVariable(configOption);
      environmentVariables.add(
          createEnvVariable(
              StaticConfig.STREAMOUT_ENV_PREFIX,
              configOptionAsEnvVariable,
              streamOutConfig.get(configOption).toString()));
    }

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      environmentVariables.add(
          createEnvVariable(
              StaticConfig.DATACATER_STREAMIN_CONFIG,
              "",
              objectMapper.writeValueAsString(streamInConfig)));
      environmentVariables.add(
          createEnvVariable(
              StaticConfig.DATACATER_STREAMOUT_CONFIG,
              "",
              objectMapper.writeValueAsString(streamOutConfig)));
    } catch (JsonProcessingException e) {
      throw new CreateDeploymentException(StringUtilities.wrapString(e.getMessage()));
    }

    // Set topic name of stream in and stream out
    environmentVariables.add(
        createEnvVariable(StaticConfig.STREAMIN_ENV_PREFIX, "TOPIC", streamIn.getName()));
    environmentVariables.add(
        createEnvVariable(StaticConfig.STREAMOUT_ENV_PREFIX, "TOPIC", streamOut.getName()));

    return environmentVariables;
  }

  /**
   * Transforms a configuration name to an environment variable.
   *
   * <p>Examples: - `datacater.name` becomes `DATACATER_NAME` - `datacater.middle-level.name`
   * becomes `DATACATER_MIDDLE_LEVEL_NAME`
   *
   * @param configName
   * @return config name as valid environment variable
   */
  private String transformConfigOptionToEnvVariable(String configName) {
    return configName
        // Replace all characters, except a-z A-Z and underscores, with an underscore
        .replaceAll("[^a-zA-Z_]", "_")
        // Transform entire string to upper case notation
        .toUpperCase();
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

  private Map<String, Object> nodeToMap(JsonNode node) {
    ObjectMapper om = new ObjectMapper();
    Map<String, Object> config = om.convertValue(node, new TypeReference<>() {});
    if (config == null) {
      return new HashMap<>();
    }
    return config;
  }

  private EnvVar createEnvVariable(String prefix, String name, String value) {
    return new EnvVarBuilder().withName(prefix.concat(name)).withValue(value).build();
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

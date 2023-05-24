package io.datacater.core.connector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.exceptions.*;
import io.datacater.core.stream.StreamEntity;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.*;
import javax.inject.Singleton;
import org.jboss.logging.Logger;

@Singleton
public class K8Deployment {
  private static final Logger LOGGER = Logger.getLogger(K8Deployment.class);
  private final KubernetesClient client;

  public K8Deployment(KubernetesClient client) {
    this.client = client;
  }

  public Map<String, Object> create(
      StreamEntity se, ConnectorSpec connectorSpec, UUID connectorId) {

    final String name = StaticConfig.CONNECTOR_NAME_PREFIX + connectorId;
    final int replicaCount = getDeploymentReplicaOrDefault(connectorSpec.connector());

    List<EnvVar> variables = getEnvironmentVariables(connectorSpec, connectorId);

    String connectorImage = connectorSpec.connector().get(StaticConfig.IMAGE_NODE_TEXT).toString();

    try {
      Deployment deployment =
          new DeploymentBuilder()
              .withNewMetadata()
              .withName(name)
              .addToLabels(getLabels(connectorId, connectorSpec.name()))
              .endMetadata()
              .withNewSpec()
              .withReplicas(replicaCount)
              .withMinReadySeconds(StaticConfig.EnvironmentVariables.READY_SECONDS)
              .withNewSelector()
              .addToMatchLabels(getLabels(connectorId, connectorSpec.name()))
              .endSelector()
              .withNewTemplate()
              .withNewMetadata()
              .addToLabels(getLabels(connectorId, connectorSpec.name()))
              .endMetadata()
              .withNewSpec()
              .addAllToContainers(
                  List.of(
                      connectorContainer(name, variables, connectorImage, se),
                      conConSidecarContainer(connectorSpec, se)))
              .endSpec()
              .endTemplate()
              .endSpec()
              .build();

      var deploymentResource =
          client.resource(deployment).inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE);
      deploymentResource.create();
    } catch (KubernetesClientException ex) {
      throw new CreateConnectorException(StringUtilities.wrapString(ex.getCause().getMessage()));
    }

    if (!exists(connectorId)) {
      throw new CreateConnectorException(StaticConfig.LoggerMessages.CONNECTOR_NOT_CREATED);
    }

    return getConnector(connectorId);
  }

  private static Map<String, String> getLabels(UUID connectorId, String prettyName) {
    return Map.of(
        StaticConfig.APP,
        StaticConfig.DATACATER_CONNECTOR,
        StaticConfig.CONNECTOR,
        StaticConfig.CONNECTOR_NO,
        StaticConfig.REVISION,
        StaticConfig.CONNECTOR_REV,
        StaticConfig.UUID_TEXT,
        connectorId.toString(),
        StaticConfig.CONNECTOR_NAME_TEXT,
        prettyName);
  }

  private Container conConSidecarContainer(ConnectorSpec connectorSpec, StreamEntity streamEntity) {
    List<EnvVar> envVariables = new ArrayList<>();
    envVariables.add(
        new EnvVarBuilder()
            .withName("CONCON_KAFKA_CONNECT_URI")
            .withValue("http://localhost:8083")
            .build());
    envVariables.add(
        new EnvVarBuilder().withName("CONNECT_NAME").withValue(connectorSpec.name()).build());
    envVariables.add(
        new EnvVarBuilder()
            .withName("CONNECT_CONFIG_TOPIC")
            .withValue(streamEntity.getName())
            .build());

    Map<String, String> config = (Map) connectorSpec.connector().get("config");
    for (String configName : config.keySet()) {
      envVariables.add(
          new EnvVarBuilder()
              .withName("CONNECT_CONFIG_" + configName.toUpperCase().replace(".", "_"))
              .withValue(config.get(configName).toString())
              .build());
    }

    return new ContainerBuilder(true)
        .withName(StaticConfig.CONCON_SIDECAR_NAME)
        .withImage(
            String.format(
                "%s:%s",
                StaticConfig.EnvironmentVariables.CONCON_IMAGE_NAME,
                StaticConfig.EnvironmentVariables.CONCON_IMAGE_TAG))
        .withImagePullPolicy(StaticConfig.EnvironmentVariables.PULL_POLICY)
        .withNewResources()
        .withRequests(StaticConfig.CONCON_SIDECAR_RESOURCE_REQUESTS)
        .withLimits(StaticConfig.CONCON_SIDECAR_RESOURCE_LIMITS)
        .endResources()
        .withEnv(envVariables)
        .build();
  }

  private Container connectorContainer(
      String name, List<EnvVar> variables, String image, StreamEntity streamEntity) {
    variables.add(
        new EnvVarBuilder()
            .withName("HEAP_OPTS")
            .withValue(StaticConfig.CONNECTOR_HEAP_OPTS)
            .build());
    variables.add(new EnvVarBuilder().withName("GROUP_ID").withValue(name).build());
    variables.add(
        new EnvVarBuilder()
            .withName("BOOTSTRAP_SERVERS")
            .withValue(streamEntity.getSpec().get("kafka").get("bootstrap.servers").textValue())
            .build());
    // TODO: allow to overwrite value via connector config
    variables.add(
        new EnvVarBuilder()
            .withName("CONFIG_STORAGE_TOPIC")
            .withValue("my_connect_configs")
            .build());
    // TODO: allow to overwrite value via connector config
    variables.add(
        new EnvVarBuilder()
            .withName("OFFSET_STORAGE_TOPIC")
            .withValue("my_connect_offsets")
            .build());
    // TODO: allow to overwrite value via connector config
    variables.add(
        new EnvVarBuilder()
            .withName("STATUS_STORAGE_TOPIC")
            .withValue("my_connect_statuses")
            .build());

    return new ContainerBuilder(true)
        .withName(name)
        .withImage(image)
        .withImagePullPolicy(StaticConfig.EnvironmentVariables.PULL_POLICY)
        .withEnv(variables)
        .withPorts(new ContainerPortBuilder().withContainerPort(8083).build())
        .withNewResources()
        .withRequests(StaticConfig.RESOURCE_REQUESTS)
        .withLimits(StaticConfig.RESOURCE_LIMITS)
        .endResources()
        .build();
  }

  public void delete(UUID connectorId) {
    String name = getDeploymentName(connectorId);
    List<StatusDetails> status =
        client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withName(name)
            .delete();

    // Above request will return the deleted deployment in Kubernetes. We expect the matching
    // deployment to be exactly
    // one and continue only if that is true.
    if (status.size() == 1) {
      LOGGER.info(String.format(StaticConfig.LoggerMessages.CONNECTOR_DELETED, name));
    } else {
      LOGGER.info(String.format(StaticConfig.LoggerMessages.CONNECTOR_NOT_DELETED, name));
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

  public Map<String, Object> getConnector(UUID connectorId) {
    try {
      return deploymentToMetaDataMap(
          client
              .apps()
              .deployments()
              .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
              .withName(getDeploymentName(connectorId))
              .get());
    } catch (DeploymentNotFoundException ex) {
      Map<String, Object> errorMap = new HashMap<>();
      Map<String, Object> messageMap = new HashMap<>();
      messageMap.put(StaticConfig.MESSAGE_TAG, StaticConfig.LoggerMessages.K8_DEPLOYMENT_NOT_FOUND);
      errorMap.put(StaticConfig.ERROR_TAG, messageMap);
      return errorMap;
    }
  }

  public String getConnectorReplicaIp(UUID connectorId, int replica) {
    String deploymentName = getDeploymentName(connectorId);
    Pod pod = getDeploymentPodByReplica(deploymentName, replica);

    return pod.getStatus().getPodIP();
  }

  private boolean exists(UUID connectorId) {
    return !client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withLabel(StaticConfig.UUID_TEXT, connectorId.toString())
        .list()
        .getItems()
        .isEmpty();
  }

  public String getDeploymentName(UUID connectorId) {
    List<Deployment> deployments =
        client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withLabel(StaticConfig.UUID_TEXT, connectorId.toString())
            .list()
            .getItems();
    if (deployments.isEmpty()) {
      throw new ConnectorNotFoundException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND);
    }
    return deployments.get(0).getMetadata().getName();
  }

  private List<EnvVar> getEnvironmentVariables(ConnectorSpec connectorSpec, UUID uuid) {
    List<EnvVar> environmentVariables = new ArrayList<>();

    // TODO

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

    return StaticConfig.EMPTY_STRING;
  }

  private int getDeploymentReplicaOrDefault(Map<String, Object> map) {
    int replica = StaticConfig.EnvironmentVariables.REPLICAS;

    return replica;
  }

  private int replicaNumberToArrayPosition(int replica) {
    int replicaPosition = replica;
    if (replicaPosition <= 0) {
      final String errorMessage =
          "The deployment replica you are searching for can not be less than 1";
      throw new DeploymentReplicaMismatchException(errorMessage);
    }
    // map replica number to array position
    replicaPosition--;
    return replicaPosition;
  }

  private Pod getDeploymentPodByReplica(String connectorName, int replica) {
    int replicaPosition = replicaNumberToArrayPosition(replica);

    final Map<String, String> matchLabels =
        client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withName(connectorName)
            .get()
            .getSpec()
            .getSelector()
            .getMatchLabels();

    Pod searchedPod;
    List<Pod> allDeploymentPods = new ArrayList<>();
    try {
      allDeploymentPods =
          client
              .pods()
              .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
              .withLabels(matchLabels)
              .list()
              .getItems();

      searchedPod =
          allDeploymentPods.stream()
              .sorted((Comparator.comparing(o -> o.getMetadata().getName())))
              .toList()
              .get(replicaPosition);
    } catch (ArrayIndexOutOfBoundsException e) {
      LOGGER.info(
          String.format(
              "An error occurred while trying to get replica %s: %s", replica, e.getMessage()));
      final String errorMessage =
          String.format(
              "The deployment replica you are searching for, %s, does not match the defined replica amount of %s.",
              replica, allDeploymentPods.size());
      throw new DeploymentReplicaMismatchException(errorMessage);
    }
    return searchedPod;
  }
}

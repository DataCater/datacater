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

  public String create(
      DatacaterDeployment dcDeployment,
      PipelineEntity pe,
      StreamEntity streamIn,
      StreamEntity streamOut) {
    final String name = getName(dcDeployment.name());
    final String volumeName = name + StaticConfig.VOLUME_NAME_SUFFIX;
    k8NameSpace.create();

    if (!pe.equals(new PipelineEntity())) {
      k8ConfigMap.getOrCreate(name, pe);
    }

    if (exists(name)) {
      return name;
    }

    List<EnvVar> variables = getEnvironmentVariables(dcDeployment, streamIn, streamOut);

    var deployment =
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
                    StaticConfig.PIPELINE_REV))
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
                    StaticConfig.PIPELINE_REV))
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
                    StaticConfig.PIPELINE_REV))
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

    // TODO  need to attach stream in and stream out bootstrap.servers to env
    client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .create(deployment);
    return name;
  }

  public String getLogs(String name) {
    return client
        .apps()
        .deployments()
        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
        .withName(name)
        .getLog(true);
  }

  public void delete(String deploymentName) {
    Boolean status =
        client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withName(deploymentName)
            .delete();
    if (status) {
      LOGGER.info(StaticConfig.LoggerMessages.DEPLOYMENT_DELETED + deploymentName);
    } else {
      LOGGER.info(StaticConfig.LoggerMessages.DEPLOYMENT_NOT_DELETED + deploymentName);
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

  private boolean exists(String name) {
    return client
            .apps()
            .deployments()
            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
            .withName(name)
            .get()
        != null;
  }

  private String getName(String name) {
    if (name == null || name.isEmpty()) {
      return StaticConfig.EnvironmentVariables.DEPLOYMENT_NAME;
    }
    return name;
  }

  private List<EnvVar> getEnvironmentVariables(
      DatacaterDeployment dcDeployment, StreamEntity streamIn, StreamEntity streamOut) {

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
    variables.add(
        createEnvVariable(
            StaticConfig.PIPELINE_OUT_CONFIG_NAME, dcDeployment.spec().getPipelineOut()));
    variables.add(
        createEnvVariable(
            StaticConfig.PIPELINE_IN_CONFIG_NAME, dcDeployment.spec().getPipelineIn()));
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

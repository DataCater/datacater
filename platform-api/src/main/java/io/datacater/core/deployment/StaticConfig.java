package io.datacater.core.deployment;

import io.fabric8.kubernetes.api.model.Quantity;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.ConfigProvider;

public class StaticConfig {
  private StaticConfig() {}

  static final String STREAMIN_ENV_PREFIX = "MP_MESSAGING_INCOMING_STREAMIN_";
  static final String STREAMOUT_ENV_PREFIX = "MP_MESSAGING_OUTGOING_STREAMOUT_";
  static final String DATACATER_SERDE_ENV_PREFIX = "DATACATER_SERDE_";
  static final String DATACATER_STREAMIN_CONFIG = "DATACATER_STREAMIN_CONFIG";
  static final String DATACATER_STREAMOUT_CONFIG = "DATACATER_STREAMOUT_CONFIG";
  static final String DATACATER_PIPELINE = "datacater-pipeline";
  static final String PYTHON_RUNNER_NAME = "python-runner";
  static final String APP = "datacater.io/app";
  static final String PIPELINE = "datacater.io/pipeline";
  static final String REVISION = "datacater.io/revision";
  static final String PIPELINE_NO = "1";
  static final String PIPELINE_REV = "1";
  static final String UUID_TEXT = "datacater.io/uuid";
  static final String DEPLOYMENT_NAME_TEXT = "datacater.io/name";
  static final String CONFIGMAP_MOUNT_PATH = "/usr/app/mounts";
  static final String DATA_SHARE_MOUNT_PATH = "/usr/app/data-mounts";
  public static final String MEMORY = "memory";
  public static final String CPU = "cpu";

  static final Map<String, Quantity> RESOURCE_REQUESTS =
      Map.of(
          CPU,
          new Quantity(EnvironmentVariables.DEPLOYMENT_RESOURCES_REQUESTS_CPU),
          MEMORY,
          new Quantity(EnvironmentVariables.DEPLOYMENT_RESOURCES_REQUESTS_MEMORY));
  static final Map<String, Quantity> RESOURCE_LIMITS = getLimits();

  static Map<String, Quantity> getLimits() {
    if (EnvironmentVariables.DEPLOYMENT_RESOURCES_LIMITS_CPU.isPresent()
        && !EnvironmentVariables.DEPLOYMENT_RESOURCES_LIMITS_CPU.get().isEmpty()) {
      return Map.of(
          CPU,
          new Quantity(EnvironmentVariables.DEPLOYMENT_RESOURCES_LIMITS_CPU.get()),
          MEMORY,
          new Quantity(EnvironmentVariables.DEPLOYMENT_RESOURCES_LIMITS_MEMORY));
    }
    return Map.of(MEMORY, new Quantity(EnvironmentVariables.DEPLOYMENT_RESOURCES_LIMITS_MEMORY));
  }

  static final String DEPLOYMENT_NAME_PREFIX = "datacater-deployment-";
  static final String CONFIGMAP_NAME_PREFIX = "datacater-configmap-";
  static final String CONFIGMAP_VOLUME_NAME_PREFIX = "datacater-volume-";
  static final String DATA_SHARE_VOLUME_NAME_PREFIX = "datacater-volume-data-";
  static final String SPEC = "spec";
  static final String STREAM_OUT = "stream-out";
  static final String STREAM_IN = "stream-in";
  static final String STREAM_IN_CONFIG = "stream-in-config";
  static final String STREAM_OUT_CONFIG = "stream-out-config";
  static final String SERIALIZER = "serializer";
  static final String DESERIALIZER = "deserializer";
  static final String EMPTY_STRING = "";
  static final String LOCALHOST_BOOTSTRAP_SERVER = "localhost:9092";
  static final String KAFKA_TAG = "kafka";
  static final String TOPIC_TAG = "topic";
  static final String GROUP_ID = "group.id";
  static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
  static final String KEY_DESERIALIZER = "key.deserializer";
  static final String KEY_SERIALIZER = "key.serializer";
  static final String VALUE_DESERIALIZER = "value.deserializer";
  static final String VALUE_SERIALIZER = "value.serializer";
  static final String STREAMIN_CONFIG_TEXT = "stream-in-config";
  static final String STREAMOUT_CONFIG_TEXT = "stream-out-config";
  static final String HTTP = "http";
  static final String CONDITIONS = "Conditions";
  static final String ERROR_TAG = "error";
  static final String MESSAGE_TAG = "message";
  static final String REPLICAS_TEXT = "replicas";

  static class EnvironmentVariables {
    private EnvironmentVariables() {}

    static final String NAMESPACE =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.namespace", String.class)
            .orElse("default")
            .toLowerCase();
    static final String FULL_IMAGE_NAME =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.image", String.class)
            .orElse("datacater/pipeline:2023.2");
    static final Integer READY_SECONDS =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.ready-seconds", Integer.class)
            .orElse(2);
    static final String PULL_POLICY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.pull-policy", String.class)
            .orElse("IfNotPresent");
    static final String DEPLOYMENT_HEALTH_PATH =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.health-path", String.class)
            .orElse("/q/health");
    static final String DEPLOYMENT_METRICS_PATH =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.metrics-path", String.class)
            .orElse("/q/metrics");
    static final int DEPLOYMENT_CONTAINER_PORT =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.image.container.port", Integer.class)
            .orElse(8080);
    static final String DEPLOYMENT_CONTAINER_PROTOCOL =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.image.container.protocol", String.class)
            .orElse("http");
    static final int REPLICAS =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.replicas", Integer.class)
            .orElse(1);
    static final String PYTHON_RUNNER_IMAGE_NAME =
        ConfigProvider.getConfig().getValue("datacater.pythonrunner.image.name", String.class);
    static final String PYTHON_RUNNER_IMAGE_TAG =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.pythonrunner.image.version", String.class)
            .orElse("2023.2");
    static final int PYTHON_RUNNER_CONTAINER_PORT =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.pythonrunner.image.containerPort", Integer.class)
            .orElse(50000);

    static final String DEPLOYMENT_RESOURCES_REQUESTS_MEMORY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.resources.requests.memory", String.class)
            .orElse("300Mi");
    static final String DEPLOYMENT_RESOURCES_LIMITS_MEMORY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.resources.limits.memory", String.class)
            .orElse("800Mi");
    static final String DEPLOYMENT_RESOURCES_REQUESTS_CPU =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.resources.requests.cpu", String.class)
            .orElse("0.1");
    static final Optional<String> DEPLOYMENT_RESOURCES_LIMITS_CPU =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.resources.limits.cpu", String.class);
  }

  static class LoggerMessages {
    private LoggerMessages() {}

    static final String DEPLOYMENT_DELETED = "Datacater Deployment deleted successfully: %s";
    static final String DEPLOYMENT_NOT_DELETED = "Datacater Deployment could not be deleted: %s";
    static final String DEPLOYMENT_NOT_FOUND = "The referenced Deployment could not be found.";
    static final String K8_DEPLOYMENT_NOT_FOUND =
        "The referenced Kubernetes Deployment could not be found.";
    static final String DEPLOYMENT_NOT_CREATED = "The Deployment could not be created.";
    static final String DEPLOYMENT_NOT_UPDATED = "The Deployment could not be updated.";
    static final String DEPLOYMENT_CREATION_FAILED =
        "An error has occurred in %s: %s %s The deployment that was failed to be created was defined as follows: %s %s";
    static final String DEPLOYMENT_FETCH_FAILED =
        "An error has occurred in %s: %s %s The deployment that could not be retrieved was: %s %s";
  }
}

package io.datacater.core.deployment;

import io.fabric8.kubernetes.api.model.Quantity;
import java.util.Map;
import org.eclipse.microprofile.config.ConfigProvider;

public class StaticConfig {
  private StaticConfig() {}

  static final String STREAM_IN_CONFIG_NAME = "MP_MESSAGING_INCOMING_STREAM_IN_TOPIC";
  static final String STREAM_OUT_CONFIG_NAME = "MP_MESSAGING_OUTGOING_STREAM_OUT_TOPIC";
  static final String DATACATER_PIPELINE = "datacater-pipeline";
  static final String PYTHON_RUNNER_NAME = "python-runner";
  static final String APP = "datacater.io/app";
  static final String PIPELINE = "datacater.io/pipeline";
  static final String REVISION = "datacater.io/revision";
  static final String PIPELINE_NO = "1";
  static final String PIPELINE_REV = "1";
  static final String UUID_TEXT = "datacater.io/uuid";
  static final String DEPLOYMENT_NAME_TEXT = "datacater.io/name";
  static final String DEPLOYMENT_SERVICE_TEXT = "datacater.io/service";
  static final String CONFIGMAP_MOUNT_PATH = "/usr/app/mounts";
  static final String DATA_SHARE_MOUNT_PATH = "/usr/app/data-mounts";
  static final Map<String, Quantity> RESOURCE_REQUESTS =
      Map.of("cpu", new Quantity("0.1"), "memory", new Quantity("1.5Gi"));
  static final Map<String, Quantity> RESOURCE_LIMITS = Map.of("memory", new Quantity("4Gi"));
  static final String DEPLOYMENT_NAME_PREFIX = "datacater-deployment-";
  static final String CONFIGMAP_NAME_PREFIX = "datacater-configmap-";
  static final String CONFIGMAP_VOLUME_NAME_PREFIX = "datacater-volume-";
  static final String DATA_SHARE_VOLUME_NAME_PREFIX = "datacater-volume-data-";
  static final String SERVICE_NAME_PREFIX = "datacater-service-";
  static final String NONE = "None";
  static final String TCP_TAG = "TCP";
  static final String SPEC = "spec";
  static final String STREAM_OUT = "stream-out";
  static final String STREAM_IN = "stream-in";
  static final String STREAM_IN_CONFIG = "stream-in-config";
  static final String STREAM_OUT_CONFIG = "stream-out-config";
  static final String PIPELINE_NODE_TEXT = "pipeline";
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
  static final String DC_STREAMIN_CONFIG_TEXT = "DATACATER_STREAM_IN_CONFIG";
  static final String DC_STREAMOUT_CONFIG_TEXT = "DATACATER_STREAM_OUT_CONFIG";
  static final String HTTP = "http";
  static final String CONDITIONS = "Conditions";
  static final String ERROR_TAG = "error";
  static final String MESSAGE_TAG = "message";

  static class EnvironmentVariables {
    private EnvironmentVariables() {}

    static final String NAMESPACE =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.namespace", String.class)
            .orElse("datacater")
            .toLowerCase();
    static final String FULL_IMAGE_NAME =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.image", String.class)
            .orElse("datacater/pipeline:alpha-20221117");
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
    public static final long DEPLOYMENT_STATS_TIMEOUT =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.stats.timeout", Long.class)
            .orElse(10000L);
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
            .orElse("alpha-20221117");
    static final int PYTHON_RUNNER_CONTAINER_PORT =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.pythonrunner.image.containerPort", Integer.class)
            .orElse(50000);
  }

  static class LoggerMessages {
    private LoggerMessages() {}

    static final String DEPLOYMENT_DELETED = "DatacaterDeployment deleted successfully: %s";
    static final String DEPLOYMENT_NOT_DELETED = "DatacaterDeployment could not be deleted: %s";
    static final String PIPELINE_NOT_FOUND = "The referenced Pipeline UUID could not be found";
    static final String STREAM_NOT_FOUND = "The referenced %s UUID could not be found";
    static final String DEPLOYMENT_NOT_FOUND = "The referenced Deployment could not be found.";
    static final String K8_DEPLOYMENT_NOT_FOUND =
        "The referenced Kubernetes Deployment could not be found.";
    static final String DEPLOYMENT_NOT_CREATED = "The Deployment could not be created.";
    static final String DEPLOYMENT_NOT_UPDATED = "The Deployment could not be updated.";
  }
}

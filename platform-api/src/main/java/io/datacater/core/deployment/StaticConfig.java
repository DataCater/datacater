package io.datacater.core.deployment;

import io.fabric8.kubernetes.api.model.Quantity;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.config.ConfigProvider;

public class StaticConfig {
  private StaticConfig() {}

  static final String PULL_POLICY = "IfNotPresent";
  static final String PIPELINE_IN_CONFIG_NAME = "MP_MESSAGING_INCOMING_PIPELINE-IN_TOPIC";
  static final String PIPELINE_OUT_CONFIG_NAME = "MP_MESSAGING_OUTGOING_PIPELINE-OUT_TOPIC";
  static final int REPLICAS = 1;
  static final String DATACATER_PIPELINE = "datacater-pipeline";
  static final String APP = "datacater.io/app";
  static final String PIPELINE = "datacater.io/pipeline";
  static final String REVISION = "datacater.io/revision";
  static final String PIPELINE_NO = "1";
  static final String PIPELINE_REV = "1";
  static final String MOUNT_PATH = "/usr/app";
  static final Map<String, Quantity> RESOURCE_REQUESTS =
      Map.of("cpu", new Quantity("0.1"), "memory", new Quantity("1.5Gi"));
  static final Map<String, Quantity> RESOURCE_LIMITS = Map.of("memory", new Quantity("4Gi"));
  static final String VOLUME_NAME_SUFFIX = "-volume";
  static final String SPEC = "spec";
  static final String STREAM_OUT = "stream-out";
  static final String STREAM_IN = "stream-in";
  static final String SERIALIZER = "serializer";
  static final String DESERIALIZER = "deserializer";
  static final String EMPTY_STRING = "";
  static final String LOCALHOST_BOOTSTRAP_SERVER = "localhost:9092";
  static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
  static final String KEY_DESERIALIZER = "key.deserializer";
  static final String KEY_SERIALIZER = "key.serializer";
  static final String VALUE_DESERIALIZER = "value.deserializer";
  static final String VALUE_SERIALIZER = "value.serializer";
  static final String STREAM_IN_BOOTSTRAP_SERVER =
      "MP_MESSAGING_INCOMING_STREAM-IN_BOOTSTRAP_SERVERS";
  static final String STREAM_IN_KEY_DESERIALIZER =
      "MP_MESSAGING_INCOMING_STREAM-IN_KEY_DESERIALIZER";
  static final String STREAM_IN_VALUE_DESERIALIZER =
      "MP_MESSAGING_INCOMING_STREAM-IN_VALUE_DESERIALIZER";
  static final String STREAM_OUT_BOOTSTRAP_SERVER =
      "MP_MESSAGING_OUTGOING_STREAM-OUT_BOOTSTRAP_SERVERS";
  static final String STREAM_OUT_KEY_SERIALIZER = "MP_MESSAGING_OUTGOING_STREAM-OUT_KEY_SERIALIZER";
  static final String STREAM_OUT_VALUE_SERIALIZER =
      "MP_MESSAGING_OUTGOING_STREAM-OUT_VALUE_SERIALIZER";

  static class EnvironmentVariables {
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
            .orElse("datacater/pipeline:latest");
  }

  static class LoggerMessages {
    static final String DEPLOYMENT_DELETED = "DatacaterDeployment deleted successfully: ";
    static final String DEPLOYMENT_NOT_DELETED = "DatacaterDeployment could not be deleted: ";
    static final String NAMESPACE_DELETED = "Namespace deleted successfully: ";
    static final String NAMESPACE_NOT_DELETED = "Namespace could not be deleted: ";
  }
}

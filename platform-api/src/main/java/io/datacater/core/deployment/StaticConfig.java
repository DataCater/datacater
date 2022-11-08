package io.datacater.core.deployment;

import io.fabric8.kubernetes.api.model.Quantity;
import java.util.Map;
import org.eclipse.microprofile.config.ConfigProvider;

public class StaticConfig {
  private StaticConfig() {}

  static final String STREAM_IN_CONFIG_NAME = "MP_MESSAGING_INCOMING_STREAM-IN_TOPIC";
  static final String STREAM_OUT_CONFIG_NAME = "MP_MESSAGING_OUTGOING_STREAM-OUT_TOPIC";
  static final String DATACATER_PIPELINE = "datacater-pipeline";
  static final String APP = "datacater.io/app";
  static final String PIPELINE = "datacater.io/pipeline";
  static final String REVISION = "datacater.io/revision";
  static final String PIPELINE_NO = "1";
  static final String PIPELINE_REV = "1";
  static final String UUID_TEXT = "datacater.io/uuid";
  static final String MOUNT_PATH = "/usr/app";
  static final Map<String, Quantity> RESOURCE_REQUESTS =
      Map.of("cpu", new Quantity("0.1"), "memory", new Quantity("1.5Gi"));
  static final Map<String, Quantity> RESOURCE_LIMITS = Map.of("memory", new Quantity("4Gi"));
  static final String DEPLOYMENT_NAME_PREFIX = "datacater-deployment-";
  static final String CONFIGMAP_NAME_PREFIX = "datacater-configmap-";
  static final String VOLUME_NAME_PREFIX = "datacater-volume-";
  static final String SPEC = "spec";
  static final String STREAM_OUT = "stream-out";
  static final String STREAM_IN = "stream-in";
  static final String PIPELINE_NODE_TEXT = "pipeline";
  static final String SERIALIZER = "serializer";
  static final String DESERIALIZER = "deserializer";
  static final String EMPTY_STRING = "";
  static final String LOCALHOST_BOOTSTRAP_SERVER = "localhost:9092";
  static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
  static final String KEY_DESERIALIZER = "key.deserializer";
  static final String KEY_SERIALIZER = "key.serializer";
  static final String VALUE_DESERIALIZER = "value.deserializer";
  static final String VALUE_SERIALIZER = "value.serializer";
  static final String STREAMIN_CONFIG_TEXT = "stream-in-config";
  static final String STREAMOUT_CONFIG_TEXT = "stream-out-config";
  static final String DC_STREAMIN_CONFIG_TEXT = "DATACATER_STREAM-IN_CONFIG";
  static final String DC_STREAMOUT_CONFIG_TEXT = "DATACATER_STREAM-OUT_CONFIG";

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
            .orElse("datacater/pipeline:alpha-20221108");
    static final Integer READY_SECONDS =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.ready-seconds", Integer.class)
            .orElse(2);
    static final String PULL_POLICY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.pull-policy", String.class)
            .orElse("IfNotPresent");
    static final int REPLICAS =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.replicas", Integer.class)
            .orElse(1);
  }

  static class LoggerMessages {
    private LoggerMessages() {}

    static final String DEPLOYMENT_DELETED = "DatacaterDeployment deleted successfully: ";
    static final String DEPLOYMENT_NOT_DELETED = "DatacaterDeployment could not be deleted: ";
    static final String PIPELINE_NOT_FOUND = "The referenced Pipeline UUID could not be found";
    static final String STREAMIN_NOT_FOUND = "The referenced Stream-in UUID could not be found";
    static final String STREAMOUT_NOT_FOUND = "The referenced Stream-out UUID could not be found";
    static final String DEPLOYMENT_NOT_CREATED = "The Deployment could not be created.";
  }
}

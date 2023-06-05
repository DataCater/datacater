package io.datacater.core.connector;

import io.fabric8.kubernetes.api.model.Quantity;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.ConfigProvider;

public class StaticConfig {
  private StaticConfig() {}

  static final String DATACATER_STREAM = "datacater-stream";
  static final String APP = "datacater.io/app";
  static final String REVISION = "datacater.io/revision";
  static final String DATACATER_CONNECTOR = "datacater-connector";
  static final String CONNECTOR = "datacater.io/connector";
  static final String CONNECTOR_NO = "1";
  static final String CONNECTOR_REV = "1";
  static final String UUID_TEXT = "datacater.io/uuid";
  static final String CONNECTOR_NAME_TEXT = "datacater.io/name";
  static final String CONNECTOR_SERVICE_TEXT = "datacater.io/service";
  public static final String MEMORY = "memory";
  public static final String CPU = "cpu";
  static final Map<String, Quantity> RESOURCE_REQUESTS =
      Map.of(
          CPU,
          new Quantity(EnvironmentVariables.CONNECTOR_RESOURCES_REQUESTS_CPU),
          MEMORY,
          new Quantity(EnvironmentVariables.CONNECTOR_RESOURCES_REQUESTS_MEMORY));
  static final Map<String, Quantity> RESOURCE_LIMITS =
      getLimits(
          EnvironmentVariables.CONNECTOR_RESOURCES_LIMITS_MEMORY,
          EnvironmentVariables.CONNECTOR_RESOURCES_LIMITS_CPU);

  static final Map<String, Quantity> CONCON_SIDECAR_RESOURCE_REQUESTS =
      Map.of(
          CPU,
          new Quantity(EnvironmentVariables.CONCON_SIDECAR_RESOURCES_REQUESTS_CPU),
          MEMORY,
          new Quantity(EnvironmentVariables.CONCON_SIDECAR_RESOURCES_REQUESTS_MEMORY));
  static final Map<String, Quantity> CONCON_SIDECAR_RESOURCE_LIMITS =
      getLimits(
          EnvironmentVariables.CONCON_SIDECAR_RESOURCES_LIMITS_MEMORY,
          EnvironmentVariables.CONCON_SIDECAR_RESOURCES_LIMITS_CPU);

  static Map<String, Quantity> getLimits(String memoryLimits, Optional<String> cpuLimits) {
    if (cpuLimits.isPresent() && !cpuLimits.get().isEmpty()) {
      return Map.of(CPU, new Quantity(cpuLimits.get()), MEMORY, new Quantity(memoryLimits));
    }
    return Map.of(MEMORY, new Quantity(memoryLimits));
  }

  static final String CONNECTOR_NAME_PREFIX = "datacater-connector-";
  static final String TCP_TAG = "TCP";
  static final String SPEC = "spec";
  static final String STREAM_NODE_TEXT = "stream";
  static final String IMAGE_NODE_TEXT = "image";
  static final String HTTP = "http";
  static final String CONDITIONS = "Conditions";
  static final String ERROR_TAG = "error";
  static final String MESSAGE_TAG = "message";
  static final String REPLICAS_TEXT = "replicas";
  static final String CONCON_SIDECAR_NAME = "concon-sidecar";
  static final String EMPTY_STRING = "";
  static final String CONNECTOR_HEAP_OPTS = "-Xms200M -Xmx200M";

  static class EnvironmentVariables {
    private EnvironmentVariables() {}

    static final String NAMESPACE =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.namespace", String.class)
            .orElse("default")
            .toLowerCase();
    static final Integer READY_SECONDS =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.ready-seconds", Integer.class)
            .orElse(10);
    static final String PULL_POLICY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.pull-policy", String.class)
            .orElse("IfNotPresent");
    static final int REPLICAS =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.replicas", Integer.class)
            .orElse(1);
    static final String CONCON_IMAGE_NAME =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.concon.image.name", String.class)
            .orElse("datacater/concon-sidecar");
    static final String CONCON_IMAGE_TAG =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.concon.image.version", String.class)
            .orElse("latest");
    static final String CONNECTOR_RESOURCES_REQUESTS_MEMORY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.resources.requests.memory", String.class)
            .orElse("250Mi");
    static final String CONNECTOR_RESOURCES_LIMITS_MEMORY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.resources.limits.memory", String.class)
            .orElse("250Mi");
    static final String CONNECTOR_RESOURCES_REQUESTS_CPU =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.resources.requests.cpu", String.class)
            .orElse("0.1");
    static final Optional<String> CONNECTOR_RESOURCES_LIMITS_CPU =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.resources.limits.cpu", String.class);
    static final Integer CONNECTOR_KAFKA_CONNECT_PORT =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.kafka-connect.port", Integer.class)
            .orElse(8083);
    static final String CONCON_SIDECAR_RESOURCES_REQUESTS_MEMORY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.sidecar.resources.requests.memory", String.class)
            .orElse("50Mi");
    static final String CONCON_SIDECAR_RESOURCES_LIMITS_MEMORY =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.sidecar.resources.limits.memory", String.class)
            .orElse("50Mi");
    static final String CONCON_SIDECAR_RESOURCES_REQUESTS_CPU =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.sidecar.resources.requests.cpu", String.class)
            .orElse("0.1");
    static final Optional<String> CONCON_SIDECAR_RESOURCES_LIMITS_CPU =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.sidecar.resources.limits.cpu", String.class);
    static final Integer CONCON_SIDECAR_HTTP_PORT =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.sidecar.http.port", Integer.class)
            .orElse(10000);
    static final String CONCON_SIDECAR_HTTP_PROTOCOL =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.sidecar.http.protocol", String.class)
            .orElse("http");
    static final String CONNECTOR_HEALTH_PATH =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.connector.sidecar.health-path", String.class)
            .orElse("/health");
  }

  static class LoggerMessages {
    private LoggerMessages() {}

    static final String CONNECTOR_DELETED = "DataCater Deployment deleted successfully: %s";
    static final String CONNECTOR_NOT_DELETED = "DataCater Deployment could not be deleted: %s";
    static final String STREAM_NOT_FOUND = "The referenced %s UUID could not be found";
    static final String CONNECTOR_NOT_FOUND = "The referenced connector could not be found.";
    static final String K8_DEPLOYMENT_NOT_FOUND =
        "The referenced Kubernetes Deployment could not be found.";
    static final String CONNECTOR_NOT_CREATED = "The connector could not be created.";
    static final String CONNECTOR_NOT_UPDATED = "The connector could not be updated.";
    static final String NO_IMAGE_PROVIDED = "The connector does not specify spec.image.";
  }
}

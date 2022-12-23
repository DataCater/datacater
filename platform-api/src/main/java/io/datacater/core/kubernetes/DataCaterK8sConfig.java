package io.datacater.core.kubernetes;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;

@ApplicationScoped
public class DataCaterK8sConfig {

  private DataCaterK8sConfig() {}

  static final String NAME = "python-runner";

  static final Map<String, String> LABELS = Map.of("app.kubernetes.io/name", NAME);
  static final String IMAGE_NAME =
      ConfigProvider.getConfig().getValue("datacater.pythonrunner.image.name", String.class);
  static final String IMAGE_TAG =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.image.version", String.class)
          .orElse("alpha-20221117");
  static final Optional<String> IMAGE_PULL_SECRET =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.images.imagePullSecret", String.class);
  static final int CONTAINER_PORT =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.image.containerPort", Integer.class)
          .orElse(50000);

  static final String CONTAINER_PORT_NAME =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.image.containerPortName", String.class)
          .orElse("http");

  static final String PYTHON_RUNNER_SERVICE_NAME =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.serviceName", String.class)
          .orElse("pythonrunner");
  static final String NAMESPACE =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.image.namespace", String.class)
          .orElse("default");
  public static final long PYTHON_RUNNER_PREVIEW_TIMEOUT =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.preview.timeout", Long.class)
          .orElse(10000L);
  static final int PYTHON_RUNNER_REPLICAS =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.replicas", Integer.class)
          .orElse(1);

  private static final String CPU = "cpu";
  private static final String MEMORY = "memory";
  static final String PYTHONRUNNER_RESOURCES_REQUESTS_CPU =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.resources.requests.cpu", String.class)
          .orElse("0.1");
  static final String PYTHONRUNNER_RESOURCES_REQUESTS_MEMORY =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.resources.requests.memory", String.class)
          .orElse("200Mi");
  static final Optional<String> PYTHONRUNNER_RESOURCES_LIMITS_CPU =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.resources.limits.cpu", String.class);
  static final String PYTHONRUNNER_RESOURCES_LIMITS_MEMORY =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.resources.requests.memory", String.class)
          .orElse("0.5");

  static ResourceRequirements getResources() {
    Map<String, Quantity> requests =
        Map.of(
            MEMORY,
            new Quantity(PYTHONRUNNER_RESOURCES_REQUESTS_MEMORY),
            CPU,
            new Quantity(PYTHONRUNNER_RESOURCES_REQUESTS_CPU));
    Map<String, Quantity> limits =
        PYTHONRUNNER_RESOURCES_LIMITS_CPU
            .map(
                s ->
                    Map.of(
                        MEMORY,
                        new Quantity(PYTHONRUNNER_RESOURCES_LIMITS_MEMORY),
                        CPU,
                        new Quantity(s)))
            .orElseGet(() -> Map.of(MEMORY, new Quantity(PYTHONRUNNER_RESOURCES_LIMITS_MEMORY)));
    return new ResourceRequirementsBuilder().withRequests(requests).withLimits(limits).build();
  }
}

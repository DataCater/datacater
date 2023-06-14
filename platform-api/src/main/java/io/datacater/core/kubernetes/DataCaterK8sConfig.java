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
  static final String STATUS_PHASE = "status.phase";
  static final String RUNNING = "Running";

  static final Map<String, String> LABELS = Map.of("app.kubernetes.io/name", NAME);
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
  static final String EMPTY_QUEUE_INIT = "Started empty python runner queue initialization";
  static final String REFILLING_PODS =
      "Pool of Python Runners is empty. Re-filling with pods in cluster.";
  static final String POD_NAME_INFO_MESSAGE = "Returning pod with name %s for interactive usage.";
  static final String PYTHONRUNNER_RESOURCES_REQUESTS_CPU =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.resources.requests.cpu", String.class)
          .orElse("0.1");
  static final String PYTHONRUNNER_RESOURCES_REQUESTS_MEMORY =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.resources.requests.memory", String.class)
          .orElse("100Mi");
  static final Optional<String> PYTHONRUNNER_RESOURCES_LIMITS_CPU =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.resources.limits.cpu", String.class);
  static final String PYTHONRUNNER_RESOURCES_LIMITS_MEMORY =
      ConfigProvider.getConfig()
          .getOptionalValue("datacater.pythonrunner.resources.limits.memory", String.class)
          .orElse("100Mi");

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
                cpuLimits ->
                    Map.of(
                        MEMORY,
                        new Quantity(PYTHONRUNNER_RESOURCES_LIMITS_MEMORY),
                        CPU,
                        new Quantity(cpuLimits)))
            .orElseGet(() -> Map.of(MEMORY, new Quantity(PYTHONRUNNER_RESOURCES_LIMITS_MEMORY)));
    return new ResourceRequirementsBuilder().withRequests(requests).withLimits(limits).build();
  }
}

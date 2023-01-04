package io.datacater.core.kubernetes;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;

@ApplicationScoped
public class DataCaterK8sConfig {

  private DataCaterK8sConfig() {}

  static final String NAME = "python-runner";

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
}

package io.datacater.core.info;

import org.eclipse.microprofile.config.ConfigProvider;

public class StaticInformation {
  private StaticInformation() {}

  static final String API_DOCUMENTATION_PATH_PREFIX = "https://docs.datacater.io/docs/api";
  static final String STREAMS_SUFFIX = "/streams";
  static final String DEPLOYMENTS_SUFFIX = "/deployments";
  static final String CONFIGS_SUFFIX = "/configs";
  static final String PIPELINES_SUFFIX = "/pipelines";
  static final String SYSTEM_PROPERTY_OS_ARCH_TEXT = "os.arch";

  static class EnvironmentVariables {
    private EnvironmentVariables() {}

    static final String VERSION =
        ConfigProvider.getConfig()
            .getOptionalValue("quarkus.application.version", String.class)
            .orElse("");

    static final String GIT_COMMIT_VERSION =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.application.git-version", String.class)
            .orElse("");

    static final String BUILD_DATE =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.application.build-date", String.class)
            .orElse("");

    static final String CONTACT_EMAIL =
        ConfigProvider.getConfig()
            .getOptionalValue("quarkus.smallrye-openapi.info-contact-email", String.class)
            .orElse("info@datacater.io");

    static final String CONTACT_NAME =
        ConfigProvider.getConfig()
            .getOptionalValue("quarkus.smallrye-openapi.info-contact-name", String.class)
            .orElse("DataCater API Support");

    static final String CONTACT_URL =
        ConfigProvider.getConfig()
            .getOptionalValue("quarkus.smallrye-openapi.info-contact-url", String.class)
            .orElse("https://datacater.io/contact");

    static final String BASE_IMAGE =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.application.base-image", String.class)
            .orElse("");

    static final String PIPELINE_IMAGE =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.deployment.image", String.class)
            .orElse("");

    static final String PYTHON_RUNNER_IMAGE =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.pythonrunner.image.name", String.class)
            .orElse("datacater/python-runner");

    static final String PYTHON_RUNNER_IMAGE_VERSION =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.pythonrunner.image.version", String.class)
            .orElse("");
  }
}

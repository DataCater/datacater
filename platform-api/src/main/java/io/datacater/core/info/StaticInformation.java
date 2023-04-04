package io.datacater.core.info;

import org.eclipse.microprofile.config.ConfigProvider;

public class StaticInformation {
  private StaticInformation() {}

  static final String API_DOCUMENTATION_PATH_PREFIX = "https://docs.datacater.io/docs/api/";
  static final String STREAMS_TEXT = "streams";
  static final String DEPLOYMENTS_TEXT = "deployments";
  static final String CONFIGS_TEXT = "configs";
  static final String PIPELINES_TEXT = "pipelines";

  static class EnvironmentVariables {
    private EnvironmentVariables() {}

    static final String VERSION =
        ConfigProvider.getConfig()
            .getOptionalValue("quarkus.application.version", String.class)
            .orElse("");

    static final String GIT_COMMIT_VERSION =
        ConfigProvider.getConfig().getOptionalValue("quarkus.application.git-version", String.class).orElse("");

    static final String BUILD_DATE =
        ConfigProvider.getConfig().getOptionalValue("quarkus.application.build-date", String.class).orElse("");

    static final String PLATFORM =
        ConfigProvider.getConfig().getOptionalValue("quarkus.application.platform", String.class).orElse("");

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
  }
}

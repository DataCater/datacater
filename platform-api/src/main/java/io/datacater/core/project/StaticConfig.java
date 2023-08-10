package io.datacater.core.project;

import org.eclipse.microprofile.config.ConfigProvider;

public class StaticConfig {
  private StaticConfig() {}

  static final String LABELS = "labels";
  static final String PROJECT_KEY = "datacater.io/project";

  static class FormattedMessages {
    private FormattedMessages() {}
  }

  static class LoggerMessages {
    private LoggerMessages() {}

    static final String UUID_NOT_FOUND_ERROR_MESSAGE_FORMATTED = "No config found for uuid %s";
    static final String PROJECT_NOT_FOUND = "Given Project `%s` could not be found";
  }

  static class EnvironmentVariables {
    private EnvironmentVariables() {}

    static final String DEFAULT_PROJECT_NAME =
        ConfigProvider.getConfig()
            .getOptionalValue("datacater.project.default.name", String.class)
            .orElse("default")
            .toLowerCase();
  }
}

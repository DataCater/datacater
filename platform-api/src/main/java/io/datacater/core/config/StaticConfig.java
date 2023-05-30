package io.datacater.core.config;

public class StaticConfig {
  private StaticConfig() {}

  static final String LABELS = "labels";

  static class LoggerMessages {
    private LoggerMessages() {}

    static final String KIND_DOES_NOT_MATCH_EXCEPTION_MESSAGE_FORMATTED =
        "The Config kind '%s' does not match that of the given resource '%s'";
    static final String KEY_EXISTS_TWICE_EXCEPTION_MESSAGE =
        "The key '%s' was found in at least two given Configs";
    static final String UUID_NOT_FOUND_ERROR_MESSAGE_FORMATTED = "No config found for uuid %s";
  }
}

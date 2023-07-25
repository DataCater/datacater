package io.datacater.core.project;

public class StaticConfig {
  private StaticConfig() {}

  static class FormattedMessages {
    private FormattedMessages() {}
  }

  static class LoggerMessages {
    private LoggerMessages() {}
    static final String UUID_NOT_FOUND_ERROR_MESSAGE_FORMATTED = "No config found for uuid %s";
  }
}

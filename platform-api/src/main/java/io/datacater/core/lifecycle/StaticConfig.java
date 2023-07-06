package io.datacater.core.lifecycle;

public class StaticConfig {
  private StaticConfig() {}

  static class LoggerMessages {
    private LoggerMessages() {}

    static final String HAS_EMPTY_KEY = "%s has an empty key";
    static final String HAS_BEEN_ADDED = "%s has already been added";
    static final String INCORRECT_FILTER_SYNTAX = "incorrect transform syntax in: %s %s %s";
    static final String INCORRECT_TRANSFORM_SYNTAX = "incorrect transform syntax in: %s %s %s";
    static final String FILE_INVALID_YML = "file does not contain valid yaml format: %s %s %s";
    static final String FILE_INVALID_PATH =
        "file path invalid or file could not be found: %s %s %s";
  }
}

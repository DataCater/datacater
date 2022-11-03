package io.datacater.exceptions;

public class KafkaConfigurationException extends RuntimeException {
  public KafkaConfigurationException(String message) {
    super(message);
  }
}

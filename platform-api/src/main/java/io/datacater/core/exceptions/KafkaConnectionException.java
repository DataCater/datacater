package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class KafkaConnectionException extends RuntimeException {
  public KafkaConnectionException(String message) {
    super(message);
  }
}

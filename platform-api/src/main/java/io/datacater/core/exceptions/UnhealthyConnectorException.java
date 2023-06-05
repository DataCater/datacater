package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class UnhealthyConnectorException extends RuntimeException {
  public UnhealthyConnectorException(String message) {
    super(message);
  }
}

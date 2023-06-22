package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class UnhealthyDeploymentException extends RuntimeException {
  public UnhealthyDeploymentException(String message) {
    super(message);
  }
}

package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class DeploymentNotFoundException extends RuntimeException {
  public DeploymentNotFoundException(String message) {
    super(message);
  }
}

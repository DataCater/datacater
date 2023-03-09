package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class DeploymentReplicaMismatchException extends RuntimeException {
  public DeploymentReplicaMismatchException(String message) {
    super(message);
  }
}

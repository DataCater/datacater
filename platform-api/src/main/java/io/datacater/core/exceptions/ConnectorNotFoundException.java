package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class ConnectorNotFoundException extends RuntimeException {
  public ConnectorNotFoundException(String message) {
    super(message);
  }
}

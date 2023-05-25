package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class ConfigNotFoundException extends RuntimeException {
  public ConfigNotFoundException(String message) {
    super(message);
  }
}

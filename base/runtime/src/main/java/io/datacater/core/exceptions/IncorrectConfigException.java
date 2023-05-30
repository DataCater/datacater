package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class IncorrectConfigException extends RuntimeException {
  public IncorrectConfigException(String message) {
    super(message);
  }
}

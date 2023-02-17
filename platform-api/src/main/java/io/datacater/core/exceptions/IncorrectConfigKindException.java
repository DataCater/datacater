package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class IncorrectConfigKindException extends RuntimeException {
  public IncorrectConfigKindException(String message) {
    super(message);
  }
}

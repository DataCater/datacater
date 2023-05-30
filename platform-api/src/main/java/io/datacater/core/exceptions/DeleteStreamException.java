package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class DeleteStreamException extends RuntimeException {
  public DeleteStreamException(String message) {
    super(message);
  }
}

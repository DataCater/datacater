package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class StreamNotFoundException extends RuntimeException {
  public StreamNotFoundException(String message) {
    super(message);
  }
}

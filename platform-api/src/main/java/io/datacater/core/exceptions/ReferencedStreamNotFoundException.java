package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class ReferencedStreamNotFoundException extends RuntimeException {
  public ReferencedStreamNotFoundException(String message) {
    super(message);
  }
}

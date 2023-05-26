package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class PipelineNotFoundException extends RuntimeException {
  public PipelineNotFoundException(String message) {
    super(message);
  }
}

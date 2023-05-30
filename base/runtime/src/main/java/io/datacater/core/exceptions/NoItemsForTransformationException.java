package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class NoItemsForTransformationException extends RuntimeException {

  public NoItemsForTransformationException(String message) {
    super(message);
  }
}

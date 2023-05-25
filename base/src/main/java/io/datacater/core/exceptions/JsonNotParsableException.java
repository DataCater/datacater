package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class JsonNotParsableException extends RuntimeException {

  public JsonNotParsableException(String message) {
    super(message);
  }
}

package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

/**
 * Exception meant for internal usage. Should not be exposed to customers and DO NOT implement a
 * {@link javax.ws.rs.ext.ExceptionMapper} for this.
 */
@ExcludeFromGeneratedCoverageReport
public class DatacaterException extends RuntimeException {
  public DatacaterException(String message) {
    super(message);
  }
}

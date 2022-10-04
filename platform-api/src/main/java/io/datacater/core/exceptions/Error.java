package io.datacater.core.exceptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import javax.ws.rs.core.Response;

@ExcludeFromGeneratedCoverageReport
public class Error {
  @JsonProperty("statusCode")
  private int statusCode;

  @JsonProperty("status")
  private Response.Status status;

  @JsonProperty("message")
  private String message;

  private Error(int statusCode, Response.Status status, String message) {
    this.statusCode = statusCode;
    this.status = status;
    this.message = message;
  }

  @JsonCreator
  public static Error from(int statusCode, Response.Status status, String message) {
    return new Error(statusCode, status, message);
  }
}

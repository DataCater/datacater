package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@ExcludeFromGeneratedCoverageReport
@Provider
public class KafkaConnectionExceptionMapper implements ExceptionMapper<KafkaConnectionException> {

  @Override
  public Response toResponse(KafkaConnectionException exception) {
    Status status = Status.INTERNAL_SERVER_ERROR;
    Error error = Error.from(status.getStatusCode(), status, exception.getMessage());
    return Response.status(status.getStatusCode()).entity(error).build();
  }
}

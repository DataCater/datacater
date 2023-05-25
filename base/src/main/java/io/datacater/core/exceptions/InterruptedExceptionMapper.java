package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@ExcludeFromGeneratedCoverageReport
@Provider
public class InterruptedExceptionMapper implements ExceptionMapper<InterruptedException> {

  @Override
  public Response toResponse(InterruptedException exception) {
    String message = "The current thread has been interrupted.";
    Error error =
        Error.from(
            Response.Status.GATEWAY_TIMEOUT.getStatusCode(),
            Response.Status.GATEWAY_TIMEOUT,
            message);
    return Response.status(Response.Status.GATEWAY_TIMEOUT).entity(error).build();
  }
}

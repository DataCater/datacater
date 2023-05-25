package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.concurrent.ExecutionException;

@ExcludeFromGeneratedCoverageReport
@Provider
public class ExecutionExceptionMapper implements ExceptionMapper<ExecutionException> {

  public ExecutionExceptionMapper() {
    super();
  }

  @Override
  public Response toResponse(ExecutionException exception) {
    String message = "The specified Stream did not exist or could not be deleted.";
    Error error =
        Error.from(
            Response.Status.BAD_REQUEST.getStatusCode(), Response.Status.BAD_REQUEST, message);
    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }
}

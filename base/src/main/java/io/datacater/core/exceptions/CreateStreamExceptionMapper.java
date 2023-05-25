package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@ExcludeFromGeneratedCoverageReport
@Provider
public class CreateStreamExceptionMapper implements ExceptionMapper<CreateStreamException> {

  public Response toResponse(CreateStreamException exception) {
    Error error =
        Error.from(
            Response.Status.BAD_REQUEST.getStatusCode(),
            Response.Status.BAD_REQUEST,
            exception.getMessage());
    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }
}

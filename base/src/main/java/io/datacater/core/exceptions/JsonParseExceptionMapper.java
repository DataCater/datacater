package io.datacater.core.exceptions;

import com.fasterxml.jackson.core.JsonParseException;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@ExcludeFromGeneratedCoverageReport
@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

  @Override
  public Response toResponse(JsonParseException exception) {
    Error error =
        Error.from(
            Response.Status.BAD_REQUEST.getStatusCode(),
            Response.Status.BAD_REQUEST,
            exception.getOriginalMessage());
    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
  }
}

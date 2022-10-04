package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@ExcludeFromGeneratedCoverageReport
@Provider
public class PipelineNotFoundExceptionMapper implements ExceptionMapper<PipelineNotFoundException> {
  @Override
  public Response toResponse(PipelineNotFoundException exception) {
    Error error =
        Error.from(
            Response.Status.NOT_FOUND.getStatusCode(),
            Response.Status.NOT_FOUND,
            exception.getMessage());
    return Response.status(Response.Status.NOT_FOUND).entity(error).build();
  }
}

package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@ExcludeFromGeneratedCoverageReport
@Provider
public class UnhealthyDeploymentExceptionMapper
    implements ExceptionMapper<UnhealthyDeploymentException> {
  @Override
  public Response toResponse(UnhealthyDeploymentException exception) {
    Error error =
        Error.from(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            Response.Status.INTERNAL_SERVER_ERROR,
            exception.getMessage());
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
  }
}

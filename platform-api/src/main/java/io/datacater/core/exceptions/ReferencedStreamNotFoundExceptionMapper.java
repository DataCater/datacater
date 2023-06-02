package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@ExcludeFromGeneratedCoverageReport
@Provider
public class ReferencedStreamNotFoundExceptionMapper
    implements ExceptionMapper<ReferencedStreamNotFoundException> {

  @Override
  public Response toResponse(ReferencedStreamNotFoundException exception) {
    Status status = Status.BAD_REQUEST;
    Error error = Error.from(status.getStatusCode(), status, exception.getMessage());
    return Response.status(status.getStatusCode()).entity(error).build();
  }
}

package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

@ExcludeFromGeneratedCoverageReport
public class StatefulSetExceptionMapper implements ExceptionMapper<StatefulSetException> {

  @Override
  public Response toResponse(StatefulSetException exception) {
    Status expectationFailed = Status.EXPECTATION_FAILED;
    Error response =
        Error.from(expectationFailed.getStatusCode(), expectationFailed, exception.getMessage());
    return Response.status(Status.EXPECTATION_FAILED).entity(response).build();
  }
}

package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

@ExcludeFromGeneratedCoverageReport
public class NoItemsForTransformationExceptionMapper
    implements ExceptionMapper<NoItemsForTransformationException> {

  @Override
  public Response toResponse(NoItemsForTransformationException exception) {
    Status expectationFailed = Status.EXPECTATION_FAILED;
    Error error =
        Error.from(
            expectationFailed.getStatusCode(),
            expectationFailed,
            "Stream does not contain any messages to be transformed.");
    return Response.status(expectationFailed.getStatusCode()).entity(error).build();
  }
}

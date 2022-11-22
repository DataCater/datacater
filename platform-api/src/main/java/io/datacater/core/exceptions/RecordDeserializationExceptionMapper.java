package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.kafka.common.errors.RecordDeserializationException;

@ExcludeFromGeneratedCoverageReport
@Provider
public class RecordDeserializationExceptionMapper
    implements ExceptionMapper<RecordDeserializationException> {

  public Response toResponse(RecordDeserializationException exception) {
    Error error =
        Error.from(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            Response.Status.INTERNAL_SERVER_ERROR,
            exception.getMessage());
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
  }
}

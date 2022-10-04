package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.smallrye.jwt.build.JwtSignatureException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@ExcludeFromGeneratedCoverageReport
@Provider
public class JwtSignatureExceptionMapper implements ExceptionMapper<JwtSignatureException> {
  private static final Logger logger = Logger.getLogger(JwtSignatureExceptionMapper.class);

  @Context UriInfo uriInfo;

  @Override
  public Response toResponse(JwtSignatureException exception) {
    String message = "Something went wrong, please contact an administrator.";
    String exceptionMessage =
        "Someone has tried to create a JWT-Token but no private key has been defined";
    logger.error(exceptionMessage);
    Error error =
        Error.from(
            Status.INTERNAL_SERVER_ERROR.getStatusCode(), Status.INTERNAL_SERVER_ERROR, message);
    return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(error).build();
  }
}

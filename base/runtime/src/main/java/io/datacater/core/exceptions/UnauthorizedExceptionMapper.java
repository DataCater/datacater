package io.datacater.core.exceptions;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.quarkus.security.UnauthorizedException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.security.Principal;

@ExcludeFromGeneratedCoverageReport
@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

  @Context UriInfo uriInfo;

  @Context SecurityContext securityContext;

  @Override
  public Response toResponse(UnauthorizedException exception) {
    Principal principal = securityContext.getUserPrincipal();
    String message =
        String.format(
            "Pro-actively authenticated users are not authorized to access: %s. Please authenticate at `ENDPOINT`",
            uriInfo.getPath());
    if (principal != null) {
      message =
          String.format(
              "%s is not authorized to access: %s", principal.getName(), uriInfo.getPath());
    }

    Error error = Error.from(Status.UNAUTHORIZED.getStatusCode(), Status.UNAUTHORIZED, message);
    return Response.status(401).header("www-authenticate", "Bearer").entity(error).build();
  }
}

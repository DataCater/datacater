package io.datacater.core.info;

import io.smallrye.mutiny.Uni;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class InfoEndpoint {
  static final Logger LOGGER = Logger.getLogger(InfoEndpoint.class);

  @GET
  public Uni<Info> getDeployments(@Context UriInfo uriInfo) {
    final URI requestUri = uriInfo.getRequestUri();
    return Uni.createFrom().item(new Info(requestUri.toASCIIString()));
  }
}

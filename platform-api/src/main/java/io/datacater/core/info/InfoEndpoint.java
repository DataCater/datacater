package io.datacater.core.info;

import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.smallrye.mutiny.Uni;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/")
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
public class InfoEndpoint {

  @GET
  public Uni<Info> getInfo(@Context UriInfo uriInfo) {
    final URI requestUri = uriInfo.getRequestUri();
    return Uni.createFrom().item(new Info(requestUri.toASCIIString()));
  }
}

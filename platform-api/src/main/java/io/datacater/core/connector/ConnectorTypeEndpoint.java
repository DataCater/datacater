package io.datacater.core.connector;

import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path("/connector_types")
@Authenticated
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
@SecurityRequirement(name = "apiToken")
@RequestScoped
public class ConnectorTypeEndpoint {

  @GET
  public Uni<Response> getConnectorTypes() {
    return Uni.createFrom().item(Response.ok().build());
  }
}

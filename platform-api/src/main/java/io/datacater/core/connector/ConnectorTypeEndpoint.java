package io.datacater.core.connector;

import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/connector_types")
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
@RequestScoped
public class ConnectorTypeEndpoint {

  @Inject ConnectorTypeInitializer connectorTypeInitializer;

  @GET
  public Uni<Response> getConnectorTypes() {
    return Uni.createFrom()
        .item(Response.ok(connectorTypeInitializer.getLoadedConnectorTypes()).build());
  }
}

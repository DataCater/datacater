package io.datacater.core.info;

import io.smallrye.mutiny.Uni;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class InfoEndpoint {
  @GET
  public Uni<Info> getDeployments() {
    return Uni.createFrom().item(new Info());
  }
}

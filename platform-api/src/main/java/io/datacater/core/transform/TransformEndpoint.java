package io.datacater.core.transform;

import io.datacater.core.lifecycle.TransformsRepository;
import io.quarkus.security.Authenticated;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path("/transforms")
@Authenticated
@SecurityRequirement(name = "apiToken")
public class TransformEndpoint {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<TransformSpec> getTransforms() {
    return TransformsRepository.getTransforms();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{key}")
  public Optional<TransformSpec> getTransform(String key) {
    return TransformsRepository.getTransform(key);
  }
}

package io.datacater.core.filter;

import io.datacater.core.lifecycle.FiltersRepository;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@RolesAllowed("dev")
@Path("/api/alpha/filters")
@SecurityRequirement(name = "apiToken")
public class FilterEndpoint {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<FilterSpec> getFilters() {
    return FiltersRepository.getFilters();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{key}")
  public Optional<FilterSpec> getFilter(String key) {
    return FiltersRepository.getFilter(key);
  }
}

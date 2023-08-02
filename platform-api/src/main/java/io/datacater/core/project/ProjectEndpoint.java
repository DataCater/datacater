package io.datacater.core.project;

import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.datacater.core.exceptions.ProjectNotFoundException;
import io.datacater.core.utilities.JsonUtilities;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;

@Path("/projects")
@Authenticated
@Produces({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
@SecurityRequirement(name = "apiToken")
public class ProjectEndpoint {

  @Inject SessionFactory sf;

  @GET
  public Uni<List<ProjectEntity>> getAllProjects() {
    return sf.withTransaction(
        session ->
            session
                .createQuery("from ProjectEntity", ProjectEntity.class)
                .getResultList()
                .onItem()
                .ifNull()
                .continueWith(List.of()));
  }

  @GET
  @Path("{uuid}")
  public Uni<ProjectEntity> getProject(@PathParam("uuid") UUID uuid) {
    return sf.withTransaction(
        session ->
            session
                .find(ProjectEntity.class, uuid)
                .onItem()
                .ifNull()
                .failWith(
                    new ProjectNotFoundException(
                        String.format(
                            StaticConfig.LoggerMessages.UUID_NOT_FOUND_ERROR_MESSAGE_FORMATTED,
                            uuid.toString()))));
  }

  @POST
  @RequestBody
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<ProjectEntity> createProject(Project project) {
    ProjectEntity projectEntity =
        ProjectEntity.from(project.getName(), JsonUtilities.convertStringMap(project.getSpec()));

    return sf.withTransaction((session, transaction) -> session.persist(projectEntity))
        .replaceWith(projectEntity);
  }

  @PUT
  @Path("{uuid}")
  @RequestBody
  @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
  public Uni<ProjectEntity> updateProject(@PathParam("uuid") UUID uuid, Project project) {
    return sf.withTransaction(
        ((session, transaction) ->
            session
                .find(ProjectEntity.class, uuid)
                .onItem()
                .ifNotNull()
                .call(projectEntity -> session.merge(projectEntity.updateEntity(project)))
                .onItem()
                .ifNull()
                .failWith(
                    new ProjectNotFoundException(
                        String.format(
                            StaticConfig.LoggerMessages.UUID_NOT_FOUND_ERROR_MESSAGE_FORMATTED,
                            uuid.toString())))));
  }

  @DELETE
  @Path("{uuid}")
  public Uni<Response> deleteProject(@PathParam("uuid") UUID uuid) {
    return sf.withTransaction(
        ((session, transaction) ->
            session
                .find(ProjectEntity.class, uuid)
                .onItem()
                .ifNull()
                .failWith(
                    new ProjectNotFoundException(
                        String.format(
                            StaticConfig.LoggerMessages.UUID_NOT_FOUND_ERROR_MESSAGE_FORMATTED,
                            uuid.toString())))
                .onItem()
                .ifNotNull()
                .call(session::remove)
                .replaceWith(Response.ok().build())));
  }
}

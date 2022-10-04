package io.datacater.core.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.smallrye.mutiny.Uni;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.hibernate.reactive.mutiny.Mutiny;

@Path("/api/alpha/deployments")
@RolesAllowed("dev")
@Produces(MediaType.APPLICATION_JSON)
public class DeploymentEndpoint {
  @Inject Mutiny.SessionFactory sf;

  @GET
  public Uni<List<DeploymentEntity>> getDeployments() {
    return sf.withSession(
        session ->
            session.createQuery("from DeploymentEntity", DeploymentEntity.class).getResultList());
  }

  @POST
  @RequestBody
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Deployment> createDeployment(Deployment deployment) throws JsonProcessingException {
    DeploymentEntity de = new DeploymentEntity(deployment.spec());
    return sf.withTransaction(
        (session, transaction) -> session.merge(de).replaceWith(() -> deployment));
  }

  @DELETE
  public Uni<DeploymentSpec> deleteDeployment(DeploymentSpec spec) {
    CriteriaBuilder cb = sf.getCriteriaBuilder();
    CriteriaDelete<DeploymentEntity> delete = cb.createCriteriaDelete(DeploymentEntity.class);
    Root<DeploymentEntity> root = delete.from(DeploymentEntity.class);
    delete.where(cb.equal(root.get("spec"), spec));
    return sf.withTransaction(
        (session, transaction) -> session.createQuery(delete).executeUpdate().replaceWith(spec));
  }
}

package io.datacater.core.project;

import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.exceptions.ProjectNotFoundException;
import io.smallrye.mutiny.Uni;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProjectUtilities {
  @Inject DataCaterSessionFactory dsf;

  public <T> Uni<T> findProjectAndPersist(String project, Uni<T> persistResponse) {
    return dsf.withTransaction(
        ((session, transaction) ->
            session
                .createQuery("from ProjectEntity", ProjectEntity.class)
                .getResultList()
                .onItem()
                .ifNotNull()
                .transform(
                    list -> {
                      List<ProjectEntity> projectList =
                          list.stream().filter(item -> item.getName().equals(project)).toList();
                      if (projectList.isEmpty()) {
                        return null;
                      }
                      return projectList;
                    })
                .onItem()
                .ifNull()
                .failWith(
                    new ProjectNotFoundException(
                        String.format(StaticConfig.LoggerMessages.PROJECT_NOT_FOUND, project)))
                .onItem()
                .ifNotNull()
                .transform(x -> persistResponse)
                .flatMap(x -> x)));
  }
}

package io.datacater.core.project;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.datacater.core.utilities.JsonUtilities;
import io.quarkus.runtime.StartupEvent;
import java.util.HashMap;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@Singleton
public class InitDefaultProject {
  private static final Logger LOGGER = Logger.getLogger(InitDefaultProject.class);

  @Inject Mutiny.SessionFactory sf;

  @Transactional
  @ExcludeFromGeneratedCoverageReport
  public void init(@Observes StartupEvent event) {
    ProjectEntity projectEntity =
        ProjectEntity.from(
            StaticConfig.EnvironmentVariables.DEFAULT_PROJECT_NAME,
            JsonUtilities.convertStringMap(new HashMap<>()));

    sf.withTransaction((session, transaction) -> session.persist(projectEntity))
        .invoke(() -> LOGGER.info(StaticConfig.LoggerMessages.DEFAULT_PROJECT_ADDED))
        .subscribe()
        .with(
            x -> {},
            failure ->
                LOGGER.info(
                    String.format(
                        StaticConfig.LoggerMessages.DEFAULT_PROJECT_ERROR, failure.getMessage())));
  }
}

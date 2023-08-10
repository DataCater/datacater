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
        ProjectEntity.from("default", JsonUtilities.convertStringMap(new HashMap<>()));

    sf.withTransaction((session, transaction) -> session.persist(projectEntity))
        .invoke(() -> LOGGER.info("Default project entity added at startup"))
        .subscribe()
        .with(
            x -> {},
            failure ->
                LOGGER.info(
                    "something failed while adding a default project entity at startup: "
                        + failure.getMessage()));
  }
}

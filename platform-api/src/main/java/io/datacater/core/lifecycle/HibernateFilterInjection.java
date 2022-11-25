package io.datacater.core.lifecycle;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.quarkus.runtime.StartupEvent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.hibernate.Filter;
import org.hibernate.Session;

public class HibernateFilterInjection {
  @Inject EntityManager em;

  @ExcludeFromGeneratedCoverageReport
  private void injectFilter(@Observes StartupEvent event) {
    Session session = em.unwrap(Session.class);
    Filter filter = session.enableFilter("tenantFilter");
    filter.setParameter("tenantId", "DataCater");
  }
}

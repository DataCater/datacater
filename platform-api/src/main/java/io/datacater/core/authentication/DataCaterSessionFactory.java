package io.datacater.core.authentication;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;
import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.reactive.mutiny.Mutiny.Session;
import org.hibernate.reactive.mutiny.Mutiny.Transaction;
import org.jboss.logging.Logger;

@Provider
@ApplicationScoped
public class DataCaterSessionFactory {
  private static final Logger LOGGER = Logger.getLogger(DataCaterSessionFactory.class);

  @Inject Mutiny.SessionFactory sf;

  @Inject SecurityIdentity si;

  private String getPrincipal() {
    String principalName = si.getPrincipal().getName();
    if (principalName == null || principalName.isEmpty()) {
      throw new UnauthorizedException();
    }

    LOGGER.info(String.format("Detected principal with name := %s.", principalName));
    return principalName;
  }

  public <T> Uni<T> withSession(Function<Session, Uni<T>> work) {
    String nativeQuery = String.format("SET datacater.tenant = %s", getPrincipal());

    return sf.withSession(
        session -> {
          var update = session.createNativeQuery(nativeQuery).executeUpdate();
          return update.onItem().transformToUni(ignore -> work.apply(session));
        });
  }

  public <T> Uni<T> withTransaction(BiFunction<Session, Transaction, Uni<T>> work) {
    String nativeQuery = String.format("SET datacater.tenant = %s", getPrincipal());

    return sf.withTransaction(
        (session, transaction) -> {
          var update = session.createNativeQuery(nativeQuery).executeUpdate();
          return update.onItem().transformToUni(ignore -> work.apply(session, transaction));
        });
  }
}

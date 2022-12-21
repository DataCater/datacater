package io.datacater.core.authentication;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
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

  @Inject CurrentIdentityAssociation si;

  private Uni<SecurityIdentity> getPrincipal() {
    return si.getDeferredIdentity();
  }

  public <T> Uni<T> withSession(Function<Session, Uni<T>> work) {
    return getPrincipal()
        .onItem()
        .ifNull()
        .failWith(new UnauthorizedException())
        .onItem()
        .ifNotNull()
        .transformToUni(
            x ->
                sf.withSession(
                    session -> {
                      LOGGER.info(
                          String.format(
                              "Detected principal with name := %s.", x.getPrincipal().getName()));
                      var update =
                          session
                              .createNativeQuery(
                                  String.format(
                                      "SET datacater.tenant = %s", x.getPrincipal().getName()))
                              .executeUpdate();
                      return update.onItem().transformToUni(ignore -> work.apply(session));
                    }));
  }

  public <T> Uni<T> withTransaction(BiFunction<Session, Transaction, Uni<T>> work) {
    return getPrincipal()
        .onItem()
        .ifNull()
        .failWith(new UnauthorizedException())
        .onItem()
        .ifNotNull()
        .transformToUni(
            x ->
                sf.withTransaction(
                    (session, transaction) -> {
                      LOGGER.info(
                          String.format(
                              "Detected principal with name := %s.", x.getPrincipal().getName()));
                      var update =
                          session
                              .createNativeQuery(
                                  String.format(
                                      "SET datacater.tenant = %s", x.getPrincipal().getName()))
                              .executeUpdate();
                      return update
                          .onItem()
                          .transformToUni(ignore -> work.apply(session, transaction));
                    }));
  }
}

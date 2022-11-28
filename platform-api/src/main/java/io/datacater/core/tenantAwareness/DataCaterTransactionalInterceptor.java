package io.datacater.core.tenantAwareness;

import io.datacater.core.stream.StreamEndpoint;
import io.quarkus.arc.Priority;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@Interceptor
@DataCaterTransactional
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 200 + 1)
class DataCaterTransactionalInterceptor {
  private static final org.jboss.logging.Logger LOGGER = Logger.getLogger(StreamEndpoint.class);

  @Inject TenantId tenantId;

  @Inject Mutiny.SessionFactory sf;

  @AroundInvoke
  Object intercept(InvocationContext ic) {
    String sessionTenantId = tenantId.get();
    LOGGER.info("setting tenant id for session: " + sessionTenantId);
    Class<?> returnType = ic.getMethod().getReturnType();

    if (returnType != Uni.class) {
      throw new RuntimeException("only Uni return types are supported with transactional methods");
    }

    return sf.withTransaction(
        (session, transaction) ->
            session
                .createNativeQuery("SET app.current_tenant = '" + sessionTenantId + "'")
                .executeUpdate()
                .flatMap(
                    (ignore) -> {
                      try {
                        return (Uni<?>) ic.proceed();
                      } catch (Exception e) {
                        return Uni.createFrom().failure(e);
                      }
                    }));
  }
}

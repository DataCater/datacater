package io.datacater.core.authentication;

import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.quarkus.arc.Priority;
import io.quarkus.security.spi.runtime.AuthorizationController;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.interceptor.Interceptor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

// example taken from
// https://quarkus.io/guides/security-customization
@Alternative
@Priority(Interceptor.Priority.LIBRARY_AFTER)
@ApplicationScoped
@ExcludeFromGeneratedCoverageReport
public class AuthMethodController extends AuthorizationController {
  @ConfigProperty(name = "datacater.authorization", defaultValue = "false")
  boolean authorization;

  @ConfigProperty(name = "quarkus.oidc.enabled", defaultValue = "false")
  boolean oidc;

  @Override
  public boolean isAuthorizationEnabled() {
    return authorization || oidc;
  }
}

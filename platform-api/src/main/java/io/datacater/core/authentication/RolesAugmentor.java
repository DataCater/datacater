package io.datacater.core.authentication;

import io.quarkus.runtime.configuration.ProfileManager;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import java.util.function.Supplier;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RolesAugmentor implements SecurityIdentityAugmentor {

  @Override
  public int priority() {
    return SecurityIdentityAugmentor.super.priority();
  }

  @Override
  public Uni<SecurityIdentity> augment(
      SecurityIdentity identity, AuthenticationRequestContext context) {
    return Uni.createFrom().item(supply(identity));
  }

  // example taken from
  // https://quarkus.io/guides/security-customization#security-identity-customization
  private Supplier<SecurityIdentity> supply(SecurityIdentity identity) {
    if (identity.isAnonymous()) {
      QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);

      // add custom role source here
      String profile = ProfileManager.getActiveProfile().toLowerCase();

      if (profile.contains("dev") || profile.contains("test")) {
        builder.addRole(Roles.DEV.role);
        builder.setPrincipal(new QuarkusPrincipal("datacater"));
      }
      return builder::build;

    } else {
      return () -> identity;
    }
  }
}

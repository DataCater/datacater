package io.datacater.core.authentication;

import io.quarkus.arc.properties.UnlessBuildProperty;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@UnlessBuildProperty(name = "datacater.authorization.basic", stringValue = "false")
@Path("/authentication")
public class AuthenticationEndpoint {
  private static final String BEARER = "Bearer";

  @ConfigProperty(name = "mp.jwt.verify.issuer")
  private String issuer;

  @ConfigProperty(name = "smallrye.jwt.new-token.lifespan")
  private int lifespan;

  @Inject SecurityIdentity securityIdentity;

  @Inject JsonWebToken jwt;

  @POST()
  @Authenticated
  @Produces(MediaType.APPLICATION_JSON)
  @SecurityRequirement(name = "basicAuth")
  public Uni<Token> getToken() {
    return Uni.createFrom().item(new Token(BEARER, generateToken(), lifespan));
  }

  private String generateToken() throws JwtSignatureException {
    return Jwt.issuer(issuer)
        .upn(securityIdentity.getPrincipal().getName())
        .groups(securityIdentity.getRoles())
        .sign();
  }
}

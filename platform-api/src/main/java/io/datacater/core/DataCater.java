package io.datacater.core;

import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;

@OpenAPIDefinition(info = @Info(title = "DataCater API", version = "alpha"))
@SecuritySchemes(
    value = {
      @SecurityScheme(
          securitySchemeName = "basicAuth",
          type = SecuritySchemeType.HTTP,
          scheme = "basic"),
      @SecurityScheme(
          securitySchemeName = "apiToken",
          type = SecuritySchemeType.HTTP,
          in = SecuritySchemeIn.HEADER,
          scheme = "bearer",
          bearerFormat = "JWT")
    })
public class DataCater extends Application {}

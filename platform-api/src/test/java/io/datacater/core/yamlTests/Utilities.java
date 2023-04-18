package io.datacater.core.yamlTests;

import static io.restassured.config.EncoderConfig.encoderConfig;

import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utilities {

  public static final Header ACCEPT_YAML =
      new Header("Accept", YAMLMediaTypes.APPLICATION_JACKSON_YAML);
  public static final Header CONTENT_YAML =
      new Header("Content-Type", YAMLMediaTypes.APPLICATION_JACKSON_YAML);

  public static final RestAssuredConfig restAssuredConfig =
      RestAssured.config()
          .encoderConfig(
              encoderConfig()
                  .encodeContentTypeAs(YAMLMediaTypes.APPLICATION_JACKSON_YAML, ContentType.TEXT));

  static String getStringFromFile(String testResourcePath) throws IOException, URISyntaxException {
    URL streamJsonURL = ClassLoader.getSystemClassLoader().getResource(testResourcePath);

    return Files.readString(Paths.get(streamJsonURL.toURI()));
  }
}

package io.datacater.core.config;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigStreamTest {
  JsonNode configJson;
  JsonNode streamJson;
  final String baseURI = "http://localhost:8081";
  final String streamsPath = "/streams";
  final String configsPath = "/configs";

  @BeforeAll
  public void setUp() throws IOException {
    ObjectMapper mapper = new JsonMapper();

    URL configURL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/streams/stream-config-test.json");
    URL streamURL =
        ClassLoader.getSystemClassLoader().getResource("configTestFiles/streams/stream-test.json");

    configJson = mapper.readTree(configURL);
    streamJson = mapper.readTree(streamURL);
  }

  @Test
  @Order(1)
  void postConfig() {

    given()
        .header("Content-Type", "application/json")
        .body(configJson.toString())
        .baseUri(baseURI)
        .post(configsPath)
        .then()
        .statusCode(200);
  }

  @Test
  @Order(2)
  void postStream() {

    given()
        .header("Content-Type", "application/json")
        .body(streamJson.toString())
        .baseUri(baseURI)
        .post(streamsPath)
        .then()
        .statusCode(200);
  }
}

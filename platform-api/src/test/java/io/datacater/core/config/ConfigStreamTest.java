package io.datacater.core.config;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigStreamTest {
  private static final Logger LOGGER = Logger.getLogger(ConfigStreamTest.class);

  JsonNode configJson;
  JsonNode streamJson;
  UUID configUUID;
  final String baseURI = "http://localhost:8081";
  final String streamsPath = "/streams";
  final String configsPath = "/configs";
  String configUUIDPlaceholder = "configUUIDPlaceholder";

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
  void postConfig() throws JsonProcessingException {
    Response configResponse =
        given()
            .header("Content-Type", "application/json")
            .body(configJson.toString())
            .baseUri(baseURI)
            .post(configsPath);

    ObjectMapper mapper = new JsonMapper();
    ConfigEntity ce = mapper.readValue(configResponse.body().asString(), ConfigEntity.class);

    configUUID = ce.getId();

    Assertions.assertEquals(200, configResponse.getStatusCode());
  }

  @Test
  @Order(2)
  void postStream() {
    String jsonString = streamJson.toString();
    jsonString = jsonString.replace(configUUIDPlaceholder, configUUID.toString());
    LOGGER.info("adding stream");
    LOGGER.info(jsonString);
    given()
        .header("Content-Type", "application/json")
        .body(jsonString)
        .baseUri(baseURI)
        .post(streamsPath)
        .then()
        .statusCode(200);
  }
}

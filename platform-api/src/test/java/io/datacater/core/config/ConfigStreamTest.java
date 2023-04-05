package io.datacater.core.config;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.stream.StreamEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigStreamTest {
  JsonNode configJson;
  JsonNode streamJson;
  JsonNode streamJsonForUpdate;
  UUID streamId;
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
    URL streamUpdateURL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/streams/stream-test-update.json");

    configJson = mapper.readTree(configURL);
    streamJson = mapper.readTree(streamURL);
    streamJsonForUpdate = mapper.readTree(streamUpdateURL);
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
  void postStream() throws JsonProcessingException {

    RequestSpecification request = given();
    request.baseUri(baseURI);
    request.header("Content-Type", "application/json");
    request.body(streamJson.toString());
    Response response = request.post(streamsPath);

    ObjectMapper mapper = new JsonMapper();
    StreamEntity se = mapper.readValue(response.body().asString(), StreamEntity.class);
    streamId = se.getId();
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  @Order(3)
  void updateStreamWithConfig() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String streamPath = String.format("%s/%s", streamsPath, streamId.toString());

    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(streamJsonForUpdate.toString())
            .put(streamPath);

    ObjectMapper mapper = new JsonMapper();
    StreamEntity se = mapper.readValue(response.body().asString(), StreamEntity.class);

    // Properties from the config should not be merged into stream object when updating
    Assertions.assertEquals("", se.getSpec().findValue("bootstrap.servers").asText());
  }
}

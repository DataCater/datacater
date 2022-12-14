package io.datacater.core.config;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(ConfigEndpoint.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigEndpointTest {
  private static final String INVALID_JSON = "{foo{bar42{};";

  JsonNode postFirstConfigJson;
  JsonNode postSecondConfigJson;
  JsonNode postThirdConfigJson;
  JsonNode postConfigMissingNameJson;
  JsonNode postConfigUnnecessaryPropertyJson;

  @BeforeAll
  public void setUp() throws IOException {
    postFirstConfigJson = getJsonFromFile("configTestFiles/post/post-config-test1-valid.json");
    postSecondConfigJson = getJsonFromFile("configTestFiles/post/post-config-test2-valid.json");
    postThirdConfigJson = getJsonFromFile("configTestFiles/post/post-config-test3-valid.json");
    postConfigMissingNameJson =
        getJsonFromFile("configTestFiles/post/post-config-test-missing-name.json");
    postConfigUnnecessaryPropertyJson =
        getJsonFromFile("configTestFiles/post/post-config-test-unnecessary-property.json");
  }

  @Test
  @Order(1)
  void testGetEmptyConfigList() {
    // get an empty list of configs
    given().get().then().statusCode(200).and().body(containsString("[]"));
  }

  @Test
  @Order(2)
  void testPostConfigs() throws JsonProcessingException {
    // create three configs
    for (var jsonNode :
        new JsonNode[] {postFirstConfigJson, postSecondConfigJson, postThirdConfigJson}) {

      given()
          .header("Content-Type", "application/json")
          .body(jsonNode.toString())
          .post()
          .then()
          .statusCode(200);
    }
  }

  @Test
  @Order(3)
  void testPostFailMissingProperties() {
    // request with body that is missing properties inside json
    given()
        .header("Content-Type", "application/json")
        .body(postConfigMissingNameJson.toString())
        .post()
        .then()
        .statusCode(400);
  }

  @Test
  @Order(4)
  void testPostFailUnnecessaryProperties() {
    // post with body that contains unnecessary properties
    // TODO change to return 400 if body contains properties which are not specified
    given()
        .header("Content-Type", "application/json")
        .body(postConfigUnnecessaryPropertyJson.toString())
        .post()
        .then()
        .statusCode(200);
  }

  @Test
  @Order(5)
  void testGetConfigFail() {
    // get a non-existing config
    given().pathParam("uuid", UUID.randomUUID()).get("/{uuid}").then().statusCode(404);
  }

  @Test
  @Order(6)
  void testGetConfigList() throws JsonProcessingException {
    // get all configs
    Response response = given().get();

    ObjectMapper objectMapper = new ObjectMapper();

    ArrayList<ConfigEntity> receivedConfigs =
        objectMapper.readValue(
            response.getBody().asString(), new TypeReference<ArrayList<ConfigEntity>>() {});

    Assertions.assertEquals(200, response.statusCode());
    // TODO change to "3" when changing response for post with unnecessary properties inside body
    Assertions.assertEquals(4, receivedConfigs.size());
  }

  @Test
  @Order(7)
  void testDeleteConfigFail() {
    // delete a non-existing config
    given().pathParam("uuid", UUID.randomUUID()).delete("/{uuid}").then().statusCode(404);
  }

  @Test
  @Order(8)
  void testDeleteConfig() throws JsonProcessingException {
    // deletes an existing config
    ObjectMapper objectMapper = new ObjectMapper();

    String responseJson = given().get().getBody().asString();
    UUID configId =
        objectMapper
            .readValue(responseJson, new TypeReference<ArrayList<ConfigEntity>>() {})
            .get(0)
            .getId();

    given().pathParam("uuid", configId).delete("/{uuid}").then().statusCode(200);
    given().pathParam("uuid", configId).get("/{uuid}").then().statusCode(404);
  }

  @Test
  @Order(9)
  void testPut() throws JsonProcessingException {
    // updates an existing config
    ObjectMapper objectMapper = new ObjectMapper();

    String responseJson = given().get().getBody().asString();
    UUID configId =
        objectMapper
            .readValue(responseJson, new TypeReference<ArrayList<ConfigEntity>>() {})
            .get(0)
            .getId();

    given()
        .header("Content-Type", "application/json")
        .pathParam("uuid", configId)
        .body(postThirdConfigJson)
        .put("/{uuid}")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(10)
  void testPutFailMissingProperties() throws JsonProcessingException {
    // updates an existing config with missing properties inside the request body
    ObjectMapper objectMapper = new ObjectMapper();

    String responseJson = given().get().getBody().asString();
    UUID configId =
        objectMapper
            .readValue(responseJson, new TypeReference<ArrayList<ConfigEntity>>() {})
            .get(0)
            .getId();

    given()
        .header("Content-Type", "application/json")
        .pathParam("uuid", configId)
        .body(postConfigMissingNameJson.toString())
        .put("/{uuid}")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(11)
  void testPutFailUnnecessaryProperties() throws JsonProcessingException {
    // updates an existing config with unnecessary properties inside the request body
    // TODO change to return 400 if body contains properties which are not specified
    ObjectMapper objectMapper = new ObjectMapper();

    String responseJson = given().get().getBody().asString();
    UUID configId =
        objectMapper
            .readValue(responseJson, new TypeReference<ArrayList<ConfigEntity>>() {})
            .get(0)
            .getId();

    given()
        .header("Content-Type", "application/json")
        .body(postConfigUnnecessaryPropertyJson.toString())
        .pathParam("uuid", configId)
        .put("/{uuid}")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(12)
  void testPostInvalidJson() {
    given()
        .header("Content-Type", "application/json")
        .body(INVALID_JSON)
        .post()
        .then()
        .statusCode(400);
  }

  @Test
  @Order(13)
  void testPutInvalidJson() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    String responseJson = given().get().getBody().asString();
    UUID configId =
        objectMapper
            .readValue(responseJson, new TypeReference<ArrayList<ConfigEntity>>() {})
            .get(0)
            .getId();

    given()
        .header("Content-Type", "application/json")
        .body(INVALID_JSON)
        .pathParam("uuid", configId)
        .put("/{uuid}")
        .then()
        .statusCode(400);
  }

  private JsonNode getJsonFromFile(String path) throws IOException {
    URL streamJsonURL = ClassLoader.getSystemClassLoader().getResource(path);
    ObjectMapper mapper = new JsonMapper();
    return mapper.readTree(streamJsonURL);
  }
}

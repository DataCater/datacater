package io.datacater.core.config;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(ConfigEndpoint.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigEndpointTest {
  JsonNode postFirstConfigJson;
  JsonNode postSecondConfigJson;
  JsonNode postThirdConfigJson;

  List<ConfigEntity> configEntities = new ArrayList<>();

  @BeforeAll
  public void setUp() throws IOException {
    postFirstConfigJson = getJsonFromFile("configTestFiles/post/post-config-test1-valid.json");
    postSecondConfigJson = getJsonFromFile("configTestFiles/post/post-config-test2-valid.json");
    postThirdConfigJson = getJsonFromFile("configTestFiles/post/post-config-test3-valid.json");
  }

  /*
  @Test
  void getAllConfigs() {
    given().get().then().statusCode(200);
  }
   */

  @Test
  @Order(1)
  void testGetEmptyConfigList() {
    // get an empty list of configs
    Response response = given().get();
    Assertions.assertEquals(200, response.statusCode());
    Assertions.assertEquals("[]", response.getBody().asString());
  }

  @Test
  @Order(2)
  void testPostConfigs() throws JsonProcessingException {
    // create three configs
    RequestSpecification request = RestAssured.given();
    ObjectMapper objectMapper = new ObjectMapper();

    for (var jsonNode :
        new JsonNode[] {postFirstConfigJson, postSecondConfigJson, postThirdConfigJson}) {
      String json = jsonNode.toString();

      request.header("Content-Type", "application/json");
      request.body(json);

      Response response = request.post();
      Assertions.assertEquals(200, response.getStatusCode());
      configEntities.add(objectMapper.readValue(response.getBody().asString(), ConfigEntity.class));
    }
  }

  @Test
  @Order(3)
  void testGetConfigFail() {
    // try to get a non-existing config
    given().pathParam("uuid", UUID.randomUUID()).get("/{uuid}").then().statusCode(404);
  }

  @Test
  @Order(4)
  void testGetConfigList() throws JsonProcessingException {
    // get all configs
    Response response = given().get();
    Assertions.assertEquals(200, response.statusCode());

    ObjectMapper objectMapper = new ObjectMapper();

    ArrayList<ConfigEntity> receivedConfigs =
        objectMapper.readValue(
            response.getBody().asString(), new TypeReference<ArrayList<ConfigEntity>>() {});
  }

  private JsonNode getJsonFromFile(String path) throws IOException {
    URL streamJsonURL = ClassLoader.getSystemClassLoader().getResource(path);
    ObjectMapper mapper = new JsonMapper();
    return mapper.readTree(streamJsonURL);
  }
}

package io.datacater.core.config;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

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
import java.util.UUID;
import org.junit.Before;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(ConfigEndpoint.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigEndpointTest {
  JsonNode postFirstConfigJson;
  JsonNode postSecondConfigJson;
  JsonNode postThirdConfigJson;

  @Before
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
  @Order(0)
  void getEmptyConfigList() {
    Response response = given().get();
    Assertions.assertEquals(200, response.statusCode());
    Assertions.assertEquals("[]", response.body().toString());
  }

  @Test
  @Order(1)
  void testPostConfigs() {
    RequestSpecification request = RestAssured.given();

    // create three configs
    for (var jsonNode :
        new JsonNode[] {postFirstConfigJson, postSecondConfigJson, postThirdConfigJson}) {
      String json = jsonNode.toString();

      request.header("Content-Type", "application/json");
      request.body(json);

      Response response = request.post();
      Assertions.assertEquals(200, response.getStatusCode());
    }
  }

  @Test
  @Order(2)
  void getConfig() {
    given().pathParam("uuid", UUID.randomUUID()).get("/{uuid}").then().statusCode(404);
  }

  private JsonNode getJsonFromFile(String path) throws IOException {
    URL streamJsonURL = ClassLoader.getSystemClassLoader().getResource(path);
    ObjectMapper mapper = new JsonMapper();
    return mapper.readTree(streamJsonURL);
  }
}

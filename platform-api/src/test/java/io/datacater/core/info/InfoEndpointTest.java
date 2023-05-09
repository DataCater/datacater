package io.datacater.core.info;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.yamlTests.Utilities;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(InfoEndpoint.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InfoEndpointTest {

  Response response;
  Info info;

  @Test
  @Order(1)
  void testGetInfo() throws JsonProcessingException {
    final int expectedResponseCode = 200;
    ObjectMapper mapper = new JsonMapper();
    response = given().get();
    info = mapper.readValue(response.body().asString(), Info.class);

    Assertions.assertEquals(expectedResponseCode, response.getStatusCode());
  }

  @Test
  void testGetInfoAsYaml() throws JsonProcessingException {
    final int expectedResponseCode = 200;
    ObjectMapper mapper = new YAMLMapper();
    response = given().header(Utilities.ACCEPT_YAML).get();
    Info infoFromYaml = mapper.readValue(response.body().asString(), Info.class);

    Assertions.assertEquals(expectedResponseCode, response.getStatusCode());
    Assertions.assertNotNull(infoFromYaml);
  }

  @Test
  void testStreamResourceInfo() {
    Assertions.assertEquals(
        "https://docs.datacater.io/docs/api/streams",
        info.resources().streams().documentationUrl());
    Assertions.assertEquals(
        "http://localhost:8081/api/v1/streams", info.resources().streams().url());
  }

  @Test
  void testPipelineResourceInfo() {
    Assertions.assertEquals(
        "https://docs.datacater.io/docs/api/pipelines",
        info.resources().pipelines().documentationUrl());
    Assertions.assertEquals(
        "http://localhost:8081/api/v1/pipelines", info.resources().pipelines().url());
  }

  @Test
  void testDeploymentResourceInfo() {
    Assertions.assertEquals(
        "https://docs.datacater.io/docs/api/deployments",
        info.resources().deployments().documentationUrl());
    Assertions.assertEquals(
        "http://localhost:8081/api/v1/deployments", info.resources().deployments().url());
  }

  @Test
  void testConfigResourceInfo() {
    Assertions.assertEquals(
        "https://docs.datacater.io/docs/api/configs",
        info.resources().configs().documentationUrl());
    Assertions.assertEquals(
        "http://localhost:8081/api/v1/configs", info.resources().configs().url());
  }

  @Test
  void testContactInfo() {
    Assertions.assertEquals("https://datacater.io/contact", info.contact().url());
    Assertions.assertEquals("info@datacater.io", info.contact().email());
    Assertions.assertEquals("DataCater API Support", info.contact().name());
  }
}

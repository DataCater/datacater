package io.datacater.core.deployment;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(DeploymentEndpoint.class)
class DeploymentEndpointTest {
  Deployment deployment;
  JsonNode deploymentJson;

  @BeforeAll
  void setUp() throws IOException {
    URL deploymentURL =
        ClassLoader.getSystemClassLoader().getResource("deployment-test-object.json");
    ObjectMapper mapper = new JsonMapper();
    deployment = mapper.readValue(deploymentURL, Deployment.class);
    deploymentJson = mapper.readTree(deploymentURL);
  }

  @Test
  void testGetDeployments() {
    given().get().then().statusCode(200);
  }

  @Test
  void testCreateDeployment() {
    RequestSpecification request = RestAssured.given();
    String json = deploymentJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testDeleteDeployment() throws JsonProcessingException {
    RequestSpecification request = RestAssured.given();
    request.header("Content-Type", "application/json");
    String json = DeploymentSpec.serializeDeploymentSpec(deployment.spec().deployment()).toString();

    request.body(json);

    Response response = request.delete();

    Assertions.assertEquals(200, response.getStatusCode());
  }
}

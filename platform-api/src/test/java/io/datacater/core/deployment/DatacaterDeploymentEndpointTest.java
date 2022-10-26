package io.datacater.core.deployment;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithKubernetesTestServer
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatacaterDeploymentEndpointTest {
  private static final Logger LOGGER = Logger.getLogger(DatacaterDeploymentEndpointTest.class);

  String baseURI = "http://localhost:8081/api/alpha";
  String deploymentsPath = "/deployments";

  String deploymentName;

  @Test
  @Order(1)
  void testCreateDeployment() throws IOException {
    String streamsPath = "/streams";
    String pipelinesPath = "/pipelines";

    String streamInUUIDPlaceholder = "streaminUUIDPlaceholder";
    String streamOutUUIDPlaceholder = "streamoutUUIDPlaceholder";

    String streamInPath = "deploymentTests/streamin.json";
    String streamOutPath = "deploymentTests/streamout.json";
    String pipelinePath = "deploymentTests/pipeline.json";

    String expectedDeploymentName = "pipeline-test";

    // add stream in
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(streamInPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(JsonURL);
    Response responseStreamIn =
        given()
            .contentType(ContentType.JSON)
            .body(json.toString())
            .baseUri(baseURI)
            .post(streamsPath);

    StreamEntity streamIn =
        mapper.readValue(responseStreamIn.body().asString(), StreamEntity.class);

    UUID streamInUUID = streamIn.getId();

    // add stream out
    JsonURL = ClassLoader.getSystemClassLoader().getResource(streamOutPath);
    json = mapper.readTree(JsonURL);
    Response responseStreamOut =
        given()
            .contentType(ContentType.JSON)
            .body(json.toString())
            .baseUri(baseURI)
            .post(streamsPath);
    StreamEntity streamOut =
        mapper.readValue(responseStreamOut.body().asString(), StreamEntity.class);

    UUID streamOutUUID = streamOut.getId();

    // add pipeline
    JsonURL = ClassLoader.getSystemClassLoader().getResource(pipelinePath);
    json = mapper.readTree(JsonURL);
    String jsonString = json.toString();
    jsonString = jsonString.replace(streamInUUIDPlaceholder, streamInUUID.toString());
    jsonString = jsonString.replace(streamOutUUIDPlaceholder, streamOutUUID.toString());
    Response responsePipeline =
        given().contentType(ContentType.JSON).body(jsonString).baseUri(baseURI).post(pipelinesPath);
    PipelineEntity pipeline =
        mapper.readValue(responsePipeline.body().asString(), PipelineEntity.class);

    UUID pipelineUUID = pipeline.getId();

    // add deployment
    Response responseDeployment =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .pathParam("pipelineUuid", pipelineUUID)
            .post(deploymentsPath + "/{pipelineUuid}");

    deploymentName = responseDeployment.body().asString();

    Assertions.assertEquals(200, responseDeployment.getStatusCode());
    Assertions.assertEquals(expectedDeploymentName, deploymentName);
  }

  @Test
  @Order(2)
  void testGetDeployments() {
    given().baseUri(baseURI).get(deploymentsPath).then().statusCode(200);
  }

  @Test
  @Order(3)
  void testDeleteDeployment() {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .pathParam("deploymentName", deploymentName)
            .delete(deploymentsPath + "/{deploymentName}");

    Assertions.assertEquals(200, response.getStatusCode());
  }
}

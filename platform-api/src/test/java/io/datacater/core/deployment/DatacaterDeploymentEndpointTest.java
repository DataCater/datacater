package io.datacater.core.deployment;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
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
  @KubernetesTestServer KubernetesServer mockServer;

  K8Deployment k8Deployment;

  final String baseURI = "http://localhost:8081";
  final String deploymentsPath = "/deployments";
  final String streamsPath = "/streams";
  final String pipelinesPath = "/pipelines";
  final String deploymentPath = "deploymentTests/deployment.json";
  final String streamInUUIDPlaceholder = "streaminUUIDPlaceholder";
  final String streamOutUUIDPlaceholder = "streamoutUUIDPlaceholder";
  final String pipelineUUIDPlaceholder = "pipelineUUIDPlaceholder";

  UUID pipelineId;
  UUID deploymentId;

  @Test
  @Order(1)
  void testCreateDeployment() throws IOException {

    String streamInPath = "deploymentTests/streamin.json";
    String streamOutPath = "deploymentTests/streamout.json";
    String pipelinePath = "deploymentTests/pipeline.json";

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

    pipelineId = pipeline.getId();

    // add deployment
    JsonURL = ClassLoader.getSystemClassLoader().getResource(deploymentPath);
    json = mapper.readTree(JsonURL);
    jsonString = json.toString();
    jsonString = jsonString.replace(pipelineUUIDPlaceholder, pipelineId.toString());

    Response responseDeployment =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(deploymentsPath);

    DeploymentEntity deployment =
        mapper.readValue(responseDeployment.body().asString(), DeploymentEntity.class);
    deploymentId = deployment.getId();

    Assertions.assertEquals(200, responseDeployment.getStatusCode());
  }

  @Test
  @Order(2)
  void testGetDeployment() {
    given()
        .pathParam("uuid", deploymentId)
        .baseUri(baseURI)
        .get(deploymentsPath + "/{uuid}")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(3)
  void testGetDeployments() {
    given().baseUri(baseURI).get(deploymentsPath).then().statusCode(200);
  }

  // only testing that logs can be fetched.
  // Log validity  ist harder to test, since logs will always change
  @Test
  @Order(4)
  void testGetDeploymentLogs() {
    given()
        .pathParam("uuid", deploymentId)
        .baseUri(baseURI)
        .get(deploymentsPath + "/{uuid}/logs")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(5)
  void testUpdateDeployment() throws IOException {
    String pipelineUUIDPlaceholder = "pipelineUUIDPlaceholder";
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(deploymentPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(JsonURL);
    String jsonString = json.toString();
    jsonString = jsonString.replace(pipelineUUIDPlaceholder, pipelineId.toString());

    Response responseDeployment =
        given()
            .pathParam("uuid", deploymentId)
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .put(deploymentsPath + "/{uuid}");

    Assertions.assertEquals(200, responseDeployment.getStatusCode());
  }

  @Test
  @Order(6)
  void testDeleteDeployment() {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .pathParam("uuid", deploymentId)
            .delete(deploymentsPath + "/{uuid}");

    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  @Order(7)
  void testGetDeletedDeployment() {
    given()
        .pathParam("uuid", deploymentId)
        .baseUri(baseURI)
        .get(deploymentsPath + "/{uuid}")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(8)
  void testGetUnknownDeployment() {
    given()
        .pathParam("uuid", UUID.randomUUID())
        .baseUri(baseURI)
        .get(deploymentsPath + "/{uuid}")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(9)
  void testGetUnknownDeploymentLogs() {
    given()
        .pathParam("uuid", UUID.randomUUID())
        .baseUri(baseURI)
        .get(deploymentsPath + "/{uuid}/logs")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(10)
  void testWatchUnknownDeploymentLogs() {
    given()
        .pathParam("uuid", UUID.randomUUID())
        .baseUri(baseURI)
        .get(deploymentsPath + "/{uuid}/watch-logs")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(10)
  void testDeleteUnknownDeployment() {
    given()
        .contentType(ContentType.JSON)
        .baseUri(baseURI)
        .pathParam("uuid", UUID.randomUUID())
        .delete(deploymentsPath + "/{uuid}")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(11)
  void testCreateDeploymentWithNoStreamsInPipeline() throws IOException {
    String streamPath = "deploymentTests/streamin.json";
    String pipelinePath = "deploymentTests/pipeline_no_streams.json";
    String deploymentPath = "deploymentTests/deployment_with_streams.json";

    // add stream in
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(streamPath);
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

    // add pipeline
    JsonURL = ClassLoader.getSystemClassLoader().getResource(pipelinePath);
    json = mapper.readTree(JsonURL);
    String jsonString = json.toString();
    Response responsePipeline =
        given().contentType(ContentType.JSON).body(jsonString).baseUri(baseURI).post(pipelinesPath);
    PipelineEntity pipeline =
        mapper.readValue(responsePipeline.body().asString(), PipelineEntity.class);

    UUID pipelineId = pipeline.getId();

    // add deployment
    JsonURL = ClassLoader.getSystemClassLoader().getResource(deploymentPath);
    json = mapper.readTree(JsonURL);
    jsonString = json.toString();
    jsonString = jsonString.replace(pipelineUUIDPlaceholder, pipelineId.toString());
    jsonString = jsonString.replace(streamInUUIDPlaceholder, streamInUUID.toString());
    jsonString = jsonString.replace(streamOutUUIDPlaceholder, streamInUUID.toString());

    Response responseDeployment =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(deploymentsPath);

    Assertions.assertEquals(200, responseDeployment.getStatusCode());
  }

  @Test
  @Order(12)
  void testCreateDeploymentWithNoStream() throws IOException {
    String pipelinePath = "deploymentTests/pipeline_no_streams.json";

    // add pipeline
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(pipelinePath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(JsonURL);
    String jsonString = json.toString();
    Response responsePipeline =
        given().contentType(ContentType.JSON).body(jsonString).baseUri(baseURI).post(pipelinesPath);
    PipelineEntity pipeline =
        mapper.readValue(responsePipeline.body().asString(), PipelineEntity.class);

    UUID pipelineId = pipeline.getId();

    // add deployment
    JsonURL = ClassLoader.getSystemClassLoader().getResource(deploymentPath);
    json = mapper.readTree(JsonURL);
    jsonString = json.toString();
    jsonString = jsonString.replace(pipelineUUIDPlaceholder, pipelineId.toString());

    Response responseDeployment =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(deploymentsPath);

    Assertions.assertEquals(400, responseDeployment.getStatusCode());
  }

  @Test
  @Order(13)
  void testCreateDeploymentWithNoStreamOut() throws IOException {
    String streamPath = "deploymentTests/streamin.json";
    String pipelinePath = "deploymentTests/pipeline_no_streams.json";
    String deploymentPath = "deploymentTests/deployment_with_only_streamin.json";

    // add stream in
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(streamPath);
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

    // add pipeline
    JsonURL = ClassLoader.getSystemClassLoader().getResource(pipelinePath);
    json = mapper.readTree(JsonURL);
    String jsonString = json.toString();
    Response responsePipeline =
        given().contentType(ContentType.JSON).body(jsonString).baseUri(baseURI).post(pipelinesPath);
    PipelineEntity pipeline =
        mapper.readValue(responsePipeline.body().asString(), PipelineEntity.class);

    UUID pipelineId = pipeline.getId();

    // add deployment
    JsonURL = ClassLoader.getSystemClassLoader().getResource(deploymentPath);
    json = mapper.readTree(JsonURL);
    jsonString = json.toString();
    jsonString = jsonString.replace(pipelineUUIDPlaceholder, pipelineId.toString());
    jsonString = jsonString.replace(streamInUUIDPlaceholder, streamInUUID.toString());

    Response responseDeployment =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(deploymentsPath);

    Assertions.assertEquals(400, responseDeployment.getStatusCode());
  }

  @Test
  @Order(13)
  void testCreateDeploymentWithEmptySpec() throws IOException {
    String deploymentPath = "deploymentTests/deployment_with_empty_spec.json";

    // add deployment
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(deploymentPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(JsonURL);
    String jsonString = json.toString();

    Response responseDeployment =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(deploymentsPath);

    Assertions.assertEquals(400, responseDeployment.getStatusCode());
  }

  @Test
  @Order(14)
  void testCreateDeploymentWithCustomReplicas() throws IOException {
    String deploymentPath = "deploymentTests/deployment_with_custom_replicas.json";

    // add deployment
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(deploymentPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(JsonURL);
    String jsonString = json.toString();
    jsonString = jsonString.replace(pipelineUUIDPlaceholder, pipelineId.toString());

    Response responseDeployment =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(deploymentsPath);

    DeploymentEntity deployment =
        mapper.readValue(responseDeployment.body().asString(), DeploymentEntity.class);

    String k8DeploymentString =
        mockServer
            .getClient()
            .apps()
            .deployments()
            .inAnyNamespace()
            .withLabel("datacater.io/uuid", deployment.getId().toString())
            .list()
            .toString();

    LOGGER.info(
        "testCreateDeploymentWithCustomReplicas response: " + responseDeployment.body().asString());

    Assertions.assertEquals(200, responseDeployment.getStatusCode());
    Assertions.assertTrue(k8DeploymentString.contains("replicas=3"));
  }
}

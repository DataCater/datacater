package io.datacater.core.config;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
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
class ConfigDeploymentTest {
  private static final Logger LOGGER = Logger.getLogger(ConfigDeploymentTest.class);
  JsonNode configJson;
  JsonNode streamInJson;
  JsonNode streamOutJson;
  JsonNode pipelineJson;
  JsonNode deploymentJson;
  UUID pipelineUUID;
  final String baseURI = "http://localhost:8081";
  final String deploymentsPath = "/deployments";
  final String streamsPath = "/streams";
  final String pipelinesPath = "/pipelines";
  final String configsPath = "/configs";
  String streaminUUIDPlaceholder = "streaminUUIDPlaceholder";
  String streamoutUUIDPlaceholder = "streamoutUUIDPlaceholder";
  String pipelineUUIDPlaceholder = "pipelineUUIDPlaceholder";

  @BeforeAll
  public void setUp() throws IOException {
    ObjectMapper mapper = new JsonMapper();

    URL configURL =
        ClassLoader.getSystemClassLoader().getResource("configTestFiles/deployments/config.json");
    URL streamInURL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/deployments/stream-in.json");
    URL streamOutURL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/deployments/stream-out.json");
    URL pipelineURL =
        ClassLoader.getSystemClassLoader().getResource("configTestFiles/deployments/pipeline.json");
    URL deploymentURL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/deployments/deployment.json");

    configJson = mapper.readTree(configURL);
    streamInJson = mapper.readTree(streamInURL);
    streamOutJson = mapper.readTree(streamOutURL);
    pipelineJson = mapper.readTree(pipelineURL);
    deploymentJson = mapper.readTree(deploymentURL);
  }

  @Test
  @Order(1)
  void postResources() throws JsonProcessingException {
    ObjectMapper mapper = new JsonMapper();

    Response streamInResponse =
        given()
            .header("Content-Type", "application/json")
            .body(streamInJson.toString())
            .baseUri(baseURI)
            .post(streamsPath);

    Response streamOutResponse =
        given()
            .header("Content-Type", "application/json")
            .body(streamOutJson.toString())
            .baseUri(baseURI)
            .post(streamsPath);

    StreamEntity streamIn =
        mapper.readValue(streamInResponse.body().asString(), StreamEntity.class);
    StreamEntity streamOut =
        mapper.readValue(streamOutResponse.body().asString(), StreamEntity.class);

    String pipelineString = pipelineJson.toString();
    pipelineString = pipelineString.replace(streaminUUIDPlaceholder, streamIn.getId().toString());
    pipelineString = pipelineString.replace(streamoutUUIDPlaceholder, streamOut.getId().toString());

    Response pipelineResponse =
        given()
            .header("Content-Type", "application/json")
            .body(pipelineString)
            .baseUri(baseURI)
            .post(pipelinesPath);

    PipelineEntity pipeline =
        mapper.readValue(pipelineResponse.body().asString(), PipelineEntity.class);
    pipelineUUID = pipeline.getId();

    Assertions.assertEquals(200, pipelineResponse.getStatusCode());
  }

  @Test
  @Order(2)
  void postConfig() {
    String configString = configJson.toString();
    configString = configString.replace(pipelineUUIDPlaceholder, pipelineUUID.toString());
    given()
        .header("Content-Type", "application/json")
        .body(configString)
        .baseUri(baseURI)
        .post(configsPath)
        .then()
        .statusCode(200);
  }

  @Test
  @Order(3)
  void postDeployment() {
    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(deploymentJson.toString())
            .baseUri(baseURI)
            .post(deploymentsPath);
    LOGGER.info("deployment response: ");
    LOGGER.info(response.body().asString());
    Assertions.assertEquals(200, response.getStatusCode());
  }
}

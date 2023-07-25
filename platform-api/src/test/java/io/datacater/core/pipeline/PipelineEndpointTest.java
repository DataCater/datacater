package io.datacater.core.pipeline;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PipelineEndpointTest {

  final String baseURI = "http://localhost:8081";
  final String streamsPath = "/streams";
  final String pipelinesPath = "/pipelines";
  Pipeline pipeline;
  JsonNode pipelineJson;

  ObjectMapper mapper;

  @Inject
  @Channel("testJson")
  Emitter<ProducerRecord<JsonObject, JsonObject>> producer;

  @BeforeAll
  void setUp() throws IOException {
    JsonObject key = new JsonObject(Map.of("key", UUID.randomUUID()));
    JsonObject value = new JsonObject(Map.of("value", UUID.randomUUID()));

    for (int i = 0; i <= 99; i++) {
      producer.send(new ProducerRecord<>("testJson", key, value));
    }

    URL pipelineURL = ClassLoader.getSystemClassLoader().getResource("pipeline-test-object.json");
    mapper = new JsonMapper();
    pipeline = mapper.readValue(pipelineURL, Pipeline.class);
    pipelineJson = mapper.readTree(pipelineURL);
  }

  @Test
  void testGetPipelines() {
    given().baseUri(baseURI).get(pipelinesPath).then().statusCode(200);
  }

  @Test
  void testNonParsable() {
    given().baseUri(baseURI).post(pipelinesPath).then().statusCode(415);
  }

  @Test
  void testCreatePipeline() {
    RequestSpecification request = RestAssured.given();
    String json = pipelineJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.baseUri(baseURI).post(pipelinesPath);
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testGetValidPipeline() throws IOException {
    ObjectMapper mapper = new JsonMapper();
    RequestSpecification request = RestAssured.given().baseUri(baseURI);
    String json = pipelineJson.toString();
    request.header("Content-Type", "application/json");
    request.body(json);

    Response responsePost = request.post(pipelinesPath);
    PipelineEntity pe = mapper.readValue(responsePost.body().asString(), PipelineEntity.class);

    Response responseGet =
        given().baseUri(baseURI).pathParam("uuid", pe.getId()).get(pipelinesPath + "/{uuid}");

    Assertions.assertEquals(200, responseGet.getStatusCode());
    Assertions.assertTrue(responseGet.body().asString().contains(String.valueOf(pe.getId())));
  }

  @Test
  void testDeletePipeline() throws IOException {
    URL pipelineJsonURL =
        ClassLoader.getSystemClassLoader().getResource("pipeline-test-delete-object.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode pipelineJson = mapper.readTree(pipelineJsonURL);
    RequestSpecification requestPost = given().baseUri(baseURI);
    String json = pipelineJson.toString();
    requestPost.header("Content-Type", "application/json");
    requestPost.body(json);

    Response responsePost = requestPost.post(pipelinesPath);
    PipelineEntity pe = mapper.readValue(responsePost.body().asString(), PipelineEntity.class);

    RequestSpecification requestDelete =
        RestAssured.given().baseUri(baseURI).pathParam("uuid", pe.getId());
    Response response = requestDelete.delete(pipelinesPath + "/{uuid}");

    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testUpdatePipeline() throws IOException {
    URL pipelineJsonURL =
        ClassLoader.getSystemClassLoader().getResource("pipeline-test-delete-object.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode pipelineJson = mapper.readTree(pipelineJsonURL);
    RequestSpecification requestPost = given().baseUri(baseURI);
    String json = pipelineJson.toString();
    requestPost.header("Content-Type", "application/json");
    requestPost.body(json);

    Response responsePost = requestPost.post(pipelinesPath);
    PipelineEntity pe = mapper.readValue(responsePost.body().asString(), PipelineEntity.class);

    RequestSpecification request = given().baseUri(baseURI).pathParam("uuid", pe.getId());
    String jsonPut = pipelineJson.toString();

    request.header("Content-Type", "application/json");
    request.body(jsonPut);

    Response response = request.put(pipelinesPath + "/{uuid}");
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Disabled
  void testPipelineInspect() throws IOException {
    URL streamJson =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-json-format.json");
    JsonNode stream = mapper.readTree(streamJson);
    Response response =
        given()
            .baseUri(baseURI)
            .header("Content-Type", "application/json")
            .body(stream.toString())
            .post(streamsPath);

    JsonNode responseJson = mapper.readTree(response.body().asString());
    UUID uuid = UUID.fromString(responseJson.get("uuid").asText());

    Map<String, String> pipelineMetadata = pipeline.getMetadata();
    Map<String, String> pipelineProjectSelector = pipeline.getProjectSelector();
    pipelineMetadata.put("stream-in", uuid.toString());
    String name = "pipeline-inspect-test";
    Pipeline withStreamIn = Pipeline.from(name, pipelineMetadata, pipeline.getSpec(), pipelineProjectSelector);
    Response responsePipeline =
        given()
            .baseUri(baseURI)
            .header("Content-Type", "application/json")
            .body(mapper.writeValueAsString(withStreamIn))
            .post(pipelinesPath);

    PipelineEntity pe = mapper.readValue(responsePipeline.body().asString(), PipelineEntity.class);

    UUID pipelineInspectionId = pe.getId();

    Response pipelineInspection =
        given()
            .baseUri(baseURI)
            .header("Content-Type", "application/json")
            .get(pipelinesPath + String.format("/%s/inspect", pipelineInspectionId));

    Assertions.assertEquals(200, pipelineInspection.statusCode());
  }

  @Disabled
  void testPipelinePreview() throws IOException {
    URL pipelinePreview = ClassLoader.getSystemClassLoader().getResource("pipeline_preview.json");

    JsonNode pipeline = mapper.readTree(pipelinePreview);

    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(pipeline.asText())
            .baseUri(baseURI)
            .post(pipelinesPath + "/preview");

    Assertions.assertEquals(200, response.statusCode());
  }
}

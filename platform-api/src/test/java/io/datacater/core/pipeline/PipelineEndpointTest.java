package io.datacater.core.pipeline;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(PipelineEndpoint.class)
class PipelineEndpointTest {

  Pipeline pipeline;
  JsonNode pipelineJson;

  ObjectMapper mapper;

  @Inject
  @Channel("testJson")
  Emitter<ProducerRecord<JsonObject, JsonObject>> producer;

  @BeforeAll
  void setUp() throws IOException, URISyntaxException {
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
    given().get().then().statusCode(200);
  }

  @Test
  void testNonParsable() {
    given().post("/pipeline").then().statusCode(405);
  }

  @Test
  void testCreatePipeline() {
    RequestSpecification request = RestAssured.given();
    String json = pipelineJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testGetValidPipeline() throws IOException {
    ObjectMapper mapper = new JsonMapper();
    RequestSpecification request = RestAssured.given();
    String json = pipelineJson.toString();
    request.header("Content-Type", "application/json");
    request.body(json);

    Response responsePost = request.post();
    PipelineEntity pe = mapper.readValue(responsePost.body().asString(), PipelineEntity.class);

    Response responseGet = given().pathParam("uuid", pe.getId()).get("{uuid}");

    Assertions.assertEquals(200, responseGet.getStatusCode());
    Assertions.assertTrue(responseGet.body().asString().contains(String.valueOf(pe.getId())));
  }

  @Test
  void testDeletePipeline() throws IOException {
    URL pipelineJsonURL =
        ClassLoader.getSystemClassLoader().getResource("pipeline-test-delete-object.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode pipelineJson = mapper.readTree(pipelineJsonURL);
    RequestSpecification requestPost = given();
    String json = pipelineJson.toString();
    requestPost.header("Content-Type", "application/json");
    requestPost.body(json);

    Response responsePost = requestPost.post();
    PipelineEntity pe = mapper.readValue(responsePost.body().asString(), PipelineEntity.class);

    RequestSpecification requestDelete = RestAssured.given().pathParam("uuid", pe.getId());
    Response response = requestDelete.delete("{uuid}");

    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testUpdatePipeline() throws IOException {
    URL pipelineJsonURL =
        ClassLoader.getSystemClassLoader().getResource("pipeline-test-delete-object.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode pipelineJson = mapper.readTree(pipelineJsonURL);
    RequestSpecification requestPost = given();
    String json = pipelineJson.toString();
    requestPost.header("Content-Type", "application/json");
    requestPost.body(json);

    Response responsePost = requestPost.post();
    PipelineEntity pe = mapper.readValue(responsePost.body().asString(), PipelineEntity.class);

    RequestSpecification request = given().pathParam("uuid", pe.getId());
    String jsonPut = pipelineJson.toString();

    request.header("Content-Type", "application/json");
    request.body(jsonPut);

    Response response = request.put("{uuid}");
    Assertions.assertEquals(200, response.getStatusCode());
  }
}

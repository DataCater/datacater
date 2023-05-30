package io.datacater.core.stream;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(StreamEndpoint.class)
class StreamInspectJsonTest {

  @Inject
  @Channel("testStreamTopicOutJson")
  Emitter<ProducerRecord<JsonObject, JsonObject>> producer;

  UUID uuid;

  @Test
  void testJsonDeserializer()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    start();

    for (int i = 0; i <= 9; i++) {
      producer.send(
          new ProducerRecord<>(
              "testJsonDeserializer",
              buildJson("test" + i, 1000 + i),
              buildJson("test" + i, 2000 + i)));
    }

    // wait on records to finish
    CompletionStage<Void> lastMessageToWaitOn =
        producer.send(
            new ProducerRecord<>(
                "testJsonDeserializer", buildJson("test", 1000), buildJson("test", 2000)));

    lastMessageToWaitOn.toCompletableFuture().get(1000, TimeUnit.MILLISECONDS);

    Response response =
        given().pathParam("uuid", uuid.toString()).queryParam("limit", "3").get("/{uuid}/inspect");

    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertTrue(response.body().asString().contains("\"title\":\"test"));
  }

  private JsonObject buildJson(String title, int year) {
    Map<String, Object> obj = new HashMap<>();
    obj.put("year", year);
    obj.put("title", title);

    return new JsonObject(obj);
  }

  void start() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-json-deserializer.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();
    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    StreamEntity se = mapper.readValue(response.body().asString(), StreamEntity.class);
    uuid = se.getId();
  }
}

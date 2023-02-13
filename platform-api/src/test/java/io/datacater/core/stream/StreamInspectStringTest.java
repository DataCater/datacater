package io.datacater.core.stream;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(StreamEndpoint.class)
class StreamInspectStringTest {

  private static final Logger LOGGER = Logger.getLogger(StreamInspectStringTest.class);

  @Inject
  @Channel("testStreamTopicOutString")
  Emitter<ProducerRecord<String, String>> producer;

  UUID uuid;

  @Test
  void testStringDeserializer()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    start();

    for (int i = 0; i <= 300; i++) {
      producer.send(
          new ProducerRecord<>(
              "testStringDeserializer", String.format("test %d", i), String.format("test %d", i)));
    }
    CompletionStage<Void> lastMessageToWaitOn =
        producer.send(
            new ProducerRecord<>(
                "testStringDeserializer",
                String.format("test %d", 1000),
                String.format("test %d", 2000)));

    lastMessageToWaitOn.toCompletableFuture().get(1000, TimeUnit.MILLISECONDS);

    Response response =
        given()
            .pathParam("uuid", uuid.toString())
            .queryParams("limit", "3", "distributedInspect", "true")
            .get("/{uuid}/inspect");

    LOGGER.info(response.body().asString());

    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertTrue(response.body().asString().contains("test 1000"));
  }

  void start() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-string-deserializer.json");
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

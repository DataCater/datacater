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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(StreamEndpoint.class)
class StreamInspectLongTest {

  @Inject
  @Channel("testStreamTopicOutLong")
  Emitter<ProducerRecord<Long, Long>> producer;

  UUID uuid;

  @Test
  void testLongDeserializer()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    start();

    for (long i = 0L; i <= 300L; i++) {
      producer.send(new ProducerRecord<>("testLongDeserializer", i, i));
    }
    CompletionStage<Void> lastMessageToWaitOn =
        producer.send(new ProducerRecord<>("testLongDeserializer", 1000L, 2000L));

    lastMessageToWaitOn.toCompletableFuture().get(1000, TimeUnit.MILLISECONDS);

    Response response =
        given().pathParam("uuid", uuid.toString()).queryParam("limit", "3").get("/{uuid}/inspect");

    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertTrue(response.body().asString().contains("\"value\":\"test"));
  }

  void start() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-long-deserializer.json");
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

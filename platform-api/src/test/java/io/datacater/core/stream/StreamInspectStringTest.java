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
class StreamInspectStringTest {

  @Inject
  @Channel("testStreamTopicOutString")
  Emitter<ProducerRecord<String, String>> producer;

  UUID uuid;

  @Test
  void testStringDeserializer() throws IOException, InterruptedException {
    start();

    for (int i = 0; i <= 300; i++) {
      producer.send(
          new ProducerRecord<>(
              "testStringDeserializer", String.format("test %d", i), String.format("test %d", i)));
    }

    // wait on records to finish
    Thread.sleep(1000);

    Response response =
        given()
            .pathParam("uuid", uuid.toString())
            .queryParams("limit", "3", "sampleMethod", SampleMethod.UNIFORM)
            .get("/{uuid}/inspect");

    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertTrue(response.body().asString().contains("\"value\":\"test"));
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

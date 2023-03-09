package io.datacater.core.config;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiConfigStreamTest {
  static final Logger LOGGER = Logger.getLogger(MultiConfigStreamTest.class);
  JsonNode config1Json;
  JsonNode config2Json;
  JsonNode config3Json;
  JsonNode streamJson;
  JsonNode streamToFailJson;
  final String baseURI = "http://localhost:8081";
  final String streamsPath = "/streams";
  final String configsPath = "/configs";

  @BeforeAll
  public void setUp() throws IOException {
    ObjectMapper mapper = new JsonMapper();

    URL config1URL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/multiConfigStreams/stream-config1.json");

    URL config2URL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/multiConfigStreams/stream-config2.json");

    URL config3URL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/multiConfigStreams/stream-config3.json");

    URL streamURL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/multiConfigStreams/stream-test.json");

    URL streamToFailURL =
        ClassLoader.getSystemClassLoader()
            .getResource("configTestFiles/multiConfigStreams/stream-test-fail.json");

    config1Json = mapper.readTree(config1URL);
    config2Json = mapper.readTree(config2URL);
    config3Json = mapper.readTree(config3URL);
    streamJson = mapper.readTree(streamURL);
    streamToFailJson = mapper.readTree(streamToFailURL);
  }

  @Test
  @Order(1)
  void postConfigs() {

    given()
        .header("Content-Type", "application/json")
        .body(config1Json.toString())
        .baseUri(baseURI)
        .post(configsPath)
        .then()
        .statusCode(200);

    given()
        .header("Content-Type", "application/json")
        .body(config2Json.toString())
        .baseUri(baseURI)
        .post(configsPath)
        .then()
        .statusCode(200);

    given()
        .header("Content-Type", "application/json")
        .body(config3Json.toString())
        .baseUri(baseURI)
        .post(configsPath)
        .then()
        .statusCode(200);
  }

  @Test
  @Order(2)
  void postStream() throws ExecutionException, InterruptedException {
    Response response =
        given()
            .header("Content-Type", "application/json")
            .body(streamJson.toString())
            .baseUri(baseURI)
            .post(streamsPath);

    LOGGER.info("stream: " + response.body().asString());

    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    Admin admin = Admin.create(props);

    DescribeTopicsResult result = admin.describeTopics(List.of("stream-test-multi-config"));
    Map<String, KafkaFuture<TopicDescription>> values = result.topicNameValues();
    KafkaFuture<TopicDescription> topicDescription = values.get("stream-test-multi-config");
    int partitions = topicDescription.get().partitions().size();

    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals(4, partitions);
  }

  @Test
  @Order(3)
  void postStreamWithDuplicateConfigKeys() {
    given()
        .header("Content-Type", "application/json")
        .body(streamToFailJson.toString())
        .baseUri(baseURI)
        .post(streamsPath)
        .then()
        .statusCode(400);
  }
}

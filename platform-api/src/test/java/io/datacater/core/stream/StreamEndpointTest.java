package io.datacater.core.stream;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestHTTPEndpoint(StreamEndpoint.class)
class StreamEndpointTest {

  UUID alreadyCreatedID;
  public static final Header ACCEPT_YAML =
      new Header("Accept", YAMLMediaTypes.APPLICATION_JACKSON_YAML);
  public static final Header CONTENT_YAML =
      new Header("Content-Type", YAMLMediaTypes.APPLICATION_JACKSON_YAML);

  @Test
  void testCreateStreamWithJsonData() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-json-format.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    StreamEntity se = mapper.readValue(response.body().asString(), StreamEntity.class);
    alreadyCreatedID = se.getId();
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testCreateStreamWithYamlData() throws IOException, URISyntaxException {
    String yamlString = getStringFromFile("streamTestFiles/stream-test-yaml-format.yml");
    ObjectMapper mapper = new YAMLMapper();

    RequestSpecification request =
        given()
            .config(
                RestAssured.config()
                    .encoderConfig(
                        encoderConfig()
                            .encodeContentTypeAs(
                                YAMLMediaTypes.APPLICATION_JACKSON_YAML, ContentType.TEXT)));
    request.header(CONTENT_YAML).header(ACCEPT_YAML);
    request.body(yamlString);

    Response response = request.post();
    StreamEntity se = mapper.readValue(response.body().asString(), StreamEntity.class);

    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("testYaml", se.getName());
  }

  @Test
  void testCreateStreamWithExplicitDeserializer() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-explicit-deserializer.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    request.post();
    Response response = request.post();
    Assertions.assertTrue(
        response
            .body()
            .asString()
            .contains(
                "\"value.deserializer\":\"org.apache.kafka.common.serialization.StringDeserializer\""));
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testCreateStreamTwice() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-json-format-twice.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    request.post();
    Response response = request.post();
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testCreateStreamExistingTopicNoConfig() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-add-existing-topic-no-config.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    // add stream so the topic exists
    Response responseFirstAdd = request.post();
    StreamEntity se = mapper.readValue(responseFirstAdd.body().asString(), StreamEntity.class);

    // delete stream (no force) but leave topic
    RequestSpecification requestDelete = RestAssured.given().pathParam("uuid", se.getId());
    requestDelete.header("Content-Type", "application/json");
    requestDelete.delete("{uuid}");

    // readd stream with no config
    Response responseSecondAdd = request.post();

    // assert could be created with empty config
    Assertions.assertEquals(200, responseSecondAdd.getStatusCode());
  }

  @Test
  void testCreateStreamWithAvroRegistryAndSchema() throws IOException {
    URL streamAvroRegAndSchemaURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-avro-with-reg-and-schema.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamAvroRegAndSchemaURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    Assertions.assertEquals(400, response.getStatusCode());
  }

  @Test
  void testCreateStreamWithAvroNoRegistryOrSchema() throws IOException {
    URL streamAvroNoRegOrSchemaURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-avro-no-reg-or-schema.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamAvroNoRegOrSchemaURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    Assertions.assertEquals(400, response.getStatusCode());
  }

  @Test
  void testCreateStreamAvroWithSchema() throws IOException {
    URL streamAvroWithSchemaURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-avro-format-schema.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamAvroWithSchemaURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testCreateStreamAvroWithRegistry() throws IOException {
    URL streamAvroWithRegistryURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-avro-format-registry.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamAvroWithRegistryURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    Assertions.assertEquals(200, response.getStatusCode());
  }

  void testCreateStreamWithInvalidReplicationFactor() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-json-format-invalid-replication-factor.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.post();
    StreamEntity se = mapper.readValue(response.body().asString(), StreamEntity.class);
    alreadyCreatedID = se.getId();
    Assertions.assertEquals(400, response.getStatusCode());
  }

  @Test
  void testUpdateStreamThatExists() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-json-format.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given().pathParam("uuid", alreadyCreatedID);
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.put("{uuid}");
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testUpdateStreamThatDoesNotExists() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-json-format.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given().pathParam("uuid", UUID.randomUUID());
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.put("{uuid}");
    Assertions.assertEquals(400, response.getStatusCode());
  }

  @Test
  void testUpdateStreamWithWrongConfig() throws IOException {
    URL streamAvroNoRegOrSchemaURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-avro-no-reg-or-schema.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamAvroNoRegOrSchemaURL);
    RequestSpecification request = given().pathParam("uuid", alreadyCreatedID);
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response response = request.put("{uuid}");
    Assertions.assertEquals(400, response.getStatusCode());
  }

  @Test
  void testGetStreams() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader().getResource("streamTestFiles/get-stream-test.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();
    request.header("Content-Type", "application/json");
    request.body(json);

    Response responsePost = request.post();
    StreamEntity se = mapper.readValue(responsePost.body().asString(), StreamEntity.class);

    Response response = given().when().get();
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertTrue(response.body().asString().contains(se.getId().toString()));
    Assertions.assertTrue(response.body().asString().contains(se.getName()));
  }

  @Test
  void testGetValidStream() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/get-valid-stream-test.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();
    request.header("Content-Type", "application/json");
    request.body(json);

    Response responsePost = request.post();
    StreamEntity se = mapper.readValue(responsePost.body().asString(), StreamEntity.class);

    Response responseGet = given().pathParam("uuid", se.getId()).get("{uuid}");

    Assertions.assertEquals(200, responseGet.getStatusCode());
    Assertions.assertTrue(responseGet.body().asString().contains(se.getName()));
  }

  @Test
  void testGetInvalidStream() {
    given().pathParam("uuid", UUID.randomUUID()).get("{uuid}").then().statusCode(404);
  }

  @Test
  void testDeleteStreamNoForce() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader().getResource("streamTestFiles/delete-stream-test.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification requestPost = given();
    String json = streamJson.toString();
    requestPost.header("Content-Type", "application/json");
    requestPost.body(json);

    Response responsePost = requestPost.post();
    StreamEntity se = mapper.readValue(responsePost.body().asString(), StreamEntity.class);

    RequestSpecification requestDelete = RestAssured.given().pathParam("uuid", se.getId());
    requestDelete.header("Content-Type", "application/json");

    Response response = requestDelete.delete("{uuid}");
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testDeleteStreamNoForceWrongUUID() {
    RequestSpecification requestDelete = RestAssured.given().pathParam("uuid", UUID.randomUUID());
    requestDelete.header("Content-Type", "application/json");

    Response response = requestDelete.delete("{uuid}");
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testDeleteStreamWithForce() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader().getResource("streamTestFiles/delete-stream-test.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification requestPost = given();
    String json = streamJson.toString();
    requestPost.header("Content-Type", "application/json");
    requestPost.body(json);

    Response responsePost = requestPost.post();
    StreamEntity se = mapper.readValue(responsePost.body().asString(), StreamEntity.class);

    RequestSpecification requestDelete = RestAssured.given().pathParam("uuid", se.getId());
    requestDelete.header("Content-Type", "application/json");
    requestDelete.header("force", "true");

    Response response = requestDelete.delete("{uuid}");
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testDeleteStreamWithForceWrongUUID() {
    RequestSpecification requestDelete = RestAssured.given().pathParam("uuid", UUID.randomUUID());
    requestDelete.header("Content-Type", "application/json");
    requestDelete.header("force", "true");

    Response response = requestDelete.delete("{uuid}");
    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  void testInspectEmptyTopic() throws IOException {
    URL streamJsonURL =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-inspect-empty-topic.json");
    ObjectMapper mapper = new JsonMapper();
    JsonNode streamJson = mapper.readTree(streamJsonURL);
    RequestSpecification request = given();
    String json = streamJson.toString();

    request.header("Content-Type", "application/json");
    request.body(json);

    Response responsePost = request.post();
    StreamEntity se = mapper.readValue(responsePost.body().asString(), StreamEntity.class);
    UUID seID = se.getId();

    Response responseInspect = given().pathParam("uuid", seID).get("/{uuid}/inspect");
    Assertions.assertEquals(200, responseInspect.getStatusCode());
  }

  static String getStringFromFile(String testResourcePath) throws IOException, URISyntaxException {
    URL streamJsonURL = ClassLoader.getSystemClassLoader().getResource(testResourcePath);

    return Files.readString(Paths.get(streamJsonURL.toURI()));
  }
}

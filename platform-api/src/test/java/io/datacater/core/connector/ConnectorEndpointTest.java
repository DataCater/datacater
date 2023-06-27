package io.datacater.core.connector;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConnectorEndpointTest {
  private static final Logger LOGGER = Logger.getLogger(ConnectorEndpointTest.class);

  @Inject KubernetesClient client;

  final String baseURI = "http://localhost:8081";
  final String connectorsPath = "/connectors";
  final String streamsPath = "/streams";
  final String connectorPath = "connectorTests/connector.json";
  final String streamUUIDPlaceholder = "streamUUIDPlaceholder";
  final String imagePlaceholder = "imagePlaceholder";

  UUID streamId;
  UUID connectorId;

  @Test
  @Order(1)
  void testCreateConnector() throws IOException {
    String streamPath = "connectorTests/stream.json";

    // Add stream
    URL jsonURL = ClassLoader.getSystemClassLoader().getResource(streamPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(jsonURL);
    Response responseStream =
        given()
            .contentType(ContentType.JSON)
            .body(json.toString())
            .baseUri(baseURI)
            .post(streamsPath);

    StreamEntity stream = mapper.readValue(responseStream.body().asString(), StreamEntity.class);
    streamId = stream.getId();

    // Add connector
    jsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    json = mapper.readTree(jsonURL);
    String jsonString = json.toString();
    jsonString =
        jsonString
            .replace(streamUUIDPlaceholder, streamId.toString())
            .replace(imagePlaceholder, "datacater/concon-mysql-source");

    Response responseConnector =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(connectorsPath);

    ConnectorEntity connector =
        mapper.readValue(responseConnector.body().asString(), ConnectorEntity.class);
    connectorId = connector.getId();

    Assertions.assertEquals(200, responseConnector.getStatusCode());
  }

  @Test
  @Order(2)
  void testGetConnector() {
    given()
        .pathParam("uuid", connectorId)
        .baseUri(baseURI)
        .get(connectorsPath + "/{uuid}")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(3)
  void testGetConnectors() {
    given().baseUri(baseURI).get(connectorsPath).then().statusCode(200);
  }

  @Test
  @Order(4)
  void testUpdateConnector() throws IOException {
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(JsonURL);
    String jsonString = json.toString();
    jsonString =
        jsonString
            .replace(streamUUIDPlaceholder, streamId.toString())
            .replace(imagePlaceholder, "datacater/concon-pg-source");

    Response connectorDeployment =
        given()
            .pathParam("uuid", connectorId)
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .put(connectorsPath + "/{uuid}");

    Assertions.assertEquals(200, connectorDeployment.getStatusCode());
  }

  @Test
  @Order(5)
  void testDeleteConnector() {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .pathParam("uuid", connectorId)
            .delete(connectorsPath + "/{uuid}");

    Assertions.assertEquals(200, response.getStatusCode());
  }

  @Test
  @Order(6)
  void testGetDeletedConnector() {
    given()
        .pathParam("uuid", connectorId)
        .baseUri(baseURI)
        .get(connectorsPath + "/{uuid}")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(7)
  void testGetUnknownConnector() {
    given()
        .pathParam("uuid", UUID.randomUUID())
        .baseUri(baseURI)
        .get(connectorsPath + "/{uuid}")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(8)
  void testGetUnknownConnectorLogs() {
    given()
        .pathParam("uuid", UUID.randomUUID())
        .baseUri(baseURI)
        .get(connectorsPath + "/{uuid}/logs")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(9)
  void testWatchUnknownConnectorLogs() {
    given()
        .pathParam("uuid", UUID.randomUUID())
        .baseUri(baseURI)
        .get(connectorsPath + "/{uuid}/watch-logs")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(10)
  void testGetUnknownConnectorHealth() {
    given()
        .pathParam("uuid", UUID.randomUUID())
        .baseUri(baseURI)
        .get(connectorsPath + "/{uuid}/health")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(11)
  void testGetUnknownConnectorStatus() {
    given()
        .pathParam("uuid", UUID.randomUUID())
        .baseUri(baseURI)
        .get(connectorsPath + "/{uuid}/status")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(12)
  void testDeleteUnknownConnector() {
    given()
        .contentType(ContentType.JSON)
        .baseUri(baseURI)
        .pathParam("uuid", UUID.randomUUID())
        .delete(connectorsPath + "/{uuid}")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(13)
  void testCreateConnectorWithNoStream() throws IOException {
    String connectorPath = "connectorTests/connector_without_stream.json";

    URL jsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(jsonURL);
    String jsonString = json.toString();
    jsonString = jsonString.replace(imagePlaceholder, "datacater/concon-mysql-source");

    Response responseConnector =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(connectorsPath);

    Assertions.assertEquals(400, responseConnector.getStatusCode());
  }

  @Test
  @Order(14)
  void testCreateConnectorWithNoImage() throws IOException {
    String connectorPath = "connectorTests/connector_without_image.json";
    String streamPath = "connectorTests/stream.json";

    // Add stream
    URL jsonURL = ClassLoader.getSystemClassLoader().getResource(streamPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(jsonURL);
    Response responseStream =
        given()
            .contentType(ContentType.JSON)
            .body(json.toString())
            .baseUri(baseURI)
            .post(streamsPath);

    StreamEntity stream = mapper.readValue(responseStream.body().asString(), StreamEntity.class);
    LOGGER.info(responseStream.body().asString());
    streamId = stream.getId();

    // Add connector
    jsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    json = mapper.readTree(jsonURL);
    String jsonString = json.toString();
    jsonString = jsonString.replace(streamUUIDPlaceholder, streamId.toString());

    Response responseConnector =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(connectorsPath);

    Assertions.assertEquals(400, responseConnector.getStatusCode());
  }

  @Test
  @Order(15)
  void testCreateConnectorWithNoConfig() throws IOException {
    String connectorPath = "connectorTests/connector_without_config.json";
    String streamPath = "connectorTests/stream.json";

    // Add stream
    URL jsonURL = ClassLoader.getSystemClassLoader().getResource(streamPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(jsonURL);
    Response responseStream =
        given()
            .contentType(ContentType.JSON)
            .body(json.toString())
            .baseUri(baseURI)
            .post(streamsPath);

    StreamEntity stream = mapper.readValue(responseStream.body().asString(), StreamEntity.class);
    LOGGER.info(responseStream.body().asString());
    streamId = stream.getId();

    // Add connector
    jsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    json = mapper.readTree(jsonURL);
    String jsonString = json.toString();
    jsonString =
        jsonString
            .replace(streamUUIDPlaceholder, streamId.toString())
            .replace(imagePlaceholder, "datacater/concon-mysql-source");

    Response responseConnector =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(connectorsPath);

    Assertions.assertEquals(200, responseConnector.getStatusCode());
  }

  @Test
  @Order(16)
  void testCreateConnectorWithEmptySpec() throws IOException {
    String connectorPath = "connectorTests/connector_with_empty_spec.json";

    // add connector
    URL JsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    ObjectMapper mapper = new JsonMapper();
    JsonNode json = mapper.readTree(JsonURL);
    String jsonString = json.toString();

    Response responseConnector =
        given()
            .contentType(ContentType.JSON)
            .baseUri(baseURI)
            .body(jsonString)
            .post(connectorsPath);

    Assertions.assertEquals(400, responseConnector.getStatusCode());
  }
}

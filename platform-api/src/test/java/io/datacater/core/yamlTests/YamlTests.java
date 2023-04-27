package io.datacater.core.yamlTests;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.config.ConfigEndpoint;
import io.datacater.core.config.ConfigEntity;
import io.datacater.core.deployment.DeploymentEndpoint;
import io.datacater.core.deployment.DeploymentEntity;
import io.datacater.core.pipeline.PipelineEndpoint;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.StreamEndpoint;
import io.datacater.core.stream.StreamEntity;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class YamlTests {
  private static final Logger LOGGER = Logger.getLogger(YamlTests.class);
  ObjectMapper yamlMapper = new YAMLMapper();
  ObjectMapper jsonMapper = new JsonMapper();
  UUID streamUuid;
  UUID pipelineUuid;
  UUID deploymentUuid;
  UUID configUuid;

  @Nested
  @TestHTTPEndpoint(ConfigEndpoint.class)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Order(1)
  class ConfigTests {
    @Test
    @Order(1)
    void testCreateConfigWithYamlData() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createConfig.yml");

      RequestSpecification request = given().config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);

      Response response = request.post();
      ConfigEntity ce = yamlMapper.readValue(response.body().asString(), ConfigEntity.class);

      configUuid = ce.getId();

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertEquals("stream-config", ce.getName());
    }

    @Test
    @Order(1)
    void testCreateConfigWithYamlDataReturnJson() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createConfig.yml");

      RequestSpecification request = given().config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_JSON);
      request.body(yamlString);

      Response response = request.post();
      ConfigEntity ce = jsonMapper.readValue(response.body().asString(), ConfigEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(ce);
    }

    @Test
    @Order(1)
    void testCreateConfigWithJsonDataReturnYaml() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createConfig.json");

      RequestSpecification request = given().config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_JSON).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);

      Response response = request.post();
      ConfigEntity ce = yamlMapper.readValue(response.body().asString(), ConfigEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(ce);
    }

    @Test
    @Order(2)
    void testUpdateConfigWithYamlData() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateConfig.yml");

      RequestSpecification request =
          given().pathParam("uuid", configUuid).config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);

      Response response = request.put("{uuid}");
      ConfigEntity ce = yamlMapper.readValue(response.body().asString(), ConfigEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertTrue(ce.getSpec().toString().contains("localhost:9093"));
    }

    @Test
    @Order(2)
    void testUpdateConfigWithYamlDataReturnJson() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateConfig.yml");

      RequestSpecification request =
          given().pathParam("uuid", configUuid).config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_JSON);
      request.body(yamlString);

      Response response = request.put("{uuid}");
      ConfigEntity ce = jsonMapper.readValue(response.body().asString(), ConfigEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(ce);
    }

    @Test
    @Order(2)
    void testUpdateConfigWithJsonDataReturnYaml() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateConfig.json");

      RequestSpecification request =
          given().pathParam("uuid", configUuid).config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_JSON).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);

      Response response = request.put("{uuid}");
      ConfigEntity ce = yamlMapper.readValue(response.body().asString(), ConfigEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(ce);
    }
  }

  @Nested
  @TestHTTPEndpoint(StreamEndpoint.class)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Order(2)
  class StreamTests {
    @Test
    @Order(1)
    void testCreateStreamWithYamlData() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createStream.yml");

      RequestSpecification request = given().config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);

      Response response = request.post();
      StreamEntity se = yamlMapper.readValue(response.body().asString(), StreamEntity.class);

      streamUuid = se.getId();

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertEquals("testYaml", se.getName());
    }

    @Test
    @Order(1)
    void testCreateStreamWithYamlDataReturnJson() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createStream.yml");

      RequestSpecification request = given().config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_JSON);
      request.body(yamlString);

      Response response = request.post();
      StreamEntity se = jsonMapper.readValue(response.body().asString(), StreamEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(se);
    }

    @Test
    @Order(1)
    void testCreateStreamWithJsonDataReturnYaml() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createStream.json");

      RequestSpecification request = given().config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_JSON).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);

      Response response = request.post();
      StreamEntity se = yamlMapper.readValue(response.body().asString(), StreamEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(se);
    }

    @Test
    @Order(2)
    void testUpdateStreamWithYamlData() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateStream.yml");

      RequestSpecification request =
          given().pathParam("uuid", streamUuid).config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);

      Response response = request.put("{uuid}");
      StreamEntity se = yamlMapper.readValue(response.body().asString(), StreamEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertTrue(se.getSpec().toString().contains("\"replication.factor\":\"1\""));
    }

    @Test
    @Order(2)
    void testUpdateStreamWithYamlDataReturnJson() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateStream.yml");

      RequestSpecification request =
          given().pathParam("uuid", streamUuid).config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_JSON);
      request.body(yamlString);

      Response response = request.put("{uuid}");
      StreamEntity se = jsonMapper.readValue(response.body().asString(), StreamEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(se);
    }

    @Test
    @Order(2)
    void testUpdateStreamWithJsonDataReturnYaml() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateStream.json");

      RequestSpecification request =
          given().pathParam("uuid", streamUuid).config(Utilities.restAssuredConfig);
      request.header(Utilities.CONTENT_JSON).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);

      Response response = request.put("{uuid}");
      StreamEntity se = yamlMapper.readValue(response.body().asString(), StreamEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(se);
    }
  }

  @Nested
  @TestHTTPEndpoint(PipelineEndpoint.class)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Order(3)
  class PipelineTests {

    @Test
    @Order(1)
    void testCreatePipelineWithYamlData() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createPipeline.yml");
      yamlString = yamlString.replace("streamUUIDPlaceholder", streamUuid.toString());

      RequestSpecification request = RestAssured.given().config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);
      Response response = request.post();
      PipelineEntity pe = yamlMapper.readValue(response.body().asString(), PipelineEntity.class);

      pipelineUuid = pe.getId();

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertEquals("pipeline-test", pe.getName());
    }

    @Test
    @Order(1)
    void testCreatePipelineWithYamlDataReturnJson() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createPipeline.yml");
      yamlString = yamlString.replace("streamUUIDPlaceholder", streamUuid.toString());

      RequestSpecification request = RestAssured.given().config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_JSON);
      request.body(yamlString);
      Response response = request.post();
      PipelineEntity pe = jsonMapper.readValue(response.body().asString(), PipelineEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(pe);
    }

    @Test
    @Order(1)
    void testCreatePipelineWithJsonDataReturnYaml() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createPipeline.json");
      yamlString = yamlString.replace("streamUUIDPlaceholder", streamUuid.toString());

      RequestSpecification request = RestAssured.given().config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_JSON).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);
      Response response = request.post();
      PipelineEntity pe = yamlMapper.readValue(response.body().asString(), PipelineEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(pe);
    }

    @Test
    @Order(2)
    void testUpdatePipelineWithYamlData() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updatePipeline.yml");
      yamlString = yamlString.replace("streamUUIDPlaceholder", streamUuid.toString());

      RequestSpecification request =
          given().pathParam("uuid", pipelineUuid).config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);
      Response response = request.put("{uuid}");

      PipelineEntity pe = yamlMapper.readValue(response.body().asString(), PipelineEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertTrue(pe.getSpec().toString().contains("\"name\":\"First step updated\""));
    }

    @Test
    @Order(2)
    void testUpdatePipelineWithYamlDataReturnJson() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updatePipeline.yml");
      yamlString = yamlString.replace("streamUUIDPlaceholder", streamUuid.toString());

      RequestSpecification request =
          given().pathParam("uuid", pipelineUuid).config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_JSON);
      request.body(yamlString);
      Response response = request.put("{uuid}");

      PipelineEntity pe = jsonMapper.readValue(response.body().asString(), PipelineEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(pe);
    }

    @Test
    @Order(2)
    void testUpdatePipelineWithJsonDataReturnYaml() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updatePipeline.json");
      yamlString = yamlString.replace("streamUUIDPlaceholder", streamUuid.toString());

      RequestSpecification request =
          given().pathParam("uuid", pipelineUuid).config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_JSON).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);
      Response response = request.put("{uuid}");

      PipelineEntity pe = yamlMapper.readValue(response.body().asString(), PipelineEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(pe);
    }
  }

  @Nested
  @TestHTTPEndpoint(DeploymentEndpoint.class)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @Order(4)
  class DeploymentTests {

    @Test
    @Order(1)
    void testCreateDeploymentWithYamlData() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createDeployment.yml");
      yamlString = yamlString.replace("pipelineUUIDPlaceholder", pipelineUuid.toString());

      Response response =
          given()
              .config(Utilities.restAssuredConfig)
              .header(Utilities.CONTENT_YAML)
              .header(Utilities.ACCEPT_YAML)
              .body(yamlString)
              .post();

      DeploymentEntity deployment =
          yamlMapper.readValue(response.body().asString(), DeploymentEntity.class);
      deploymentUuid = deployment.getId();

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertTrue(deployment.getSpec().toString().contains("\"replicas\":1"));
    }

    @Test
    @Order(1)
    void testCreateDeploymentWithYamlDataReturnJSon() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createDeployment.yml");
      yamlString = yamlString.replace("pipelineUUIDPlaceholder", pipelineUuid.toString());

      Response response =
          given()
              .config(Utilities.restAssuredConfig)
              .header(Utilities.CONTENT_YAML)
              .header(Utilities.ACCEPT_JSON)
              .body(yamlString)
              .post();

      DeploymentEntity deployment =
          jsonMapper.readValue(response.body().asString(), DeploymentEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(deployment);
    }

    @Test
    @Order(1)
    void testCreateDeploymentWithJsonDataReturnYaml() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/createDeployment.json");
      yamlString = yamlString.replace("pipelineUUIDPlaceholder", pipelineUuid.toString());

      Response response =
          given()
              .config(Utilities.restAssuredConfig)
              .header(Utilities.CONTENT_JSON)
              .header(Utilities.ACCEPT_YAML)
              .body(yamlString)
              .post();

      DeploymentEntity deployment =
          yamlMapper.readValue(response.body().asString(), DeploymentEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(deployment);
    }

    @Test
    @Order(2)
    void testUpdateDeploymentWithYamlData() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateDeployment.yml");
      yamlString = yamlString.replace("pipelineUUIDPlaceholder", pipelineUuid.toString());

      RequestSpecification request =
          given().pathParam("uuid", deploymentUuid).config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);
      Response response = request.put("{uuid}");

      DeploymentEntity de =
          yamlMapper.readValue(response.body().asString(), DeploymentEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertTrue(de.getSpec().toString().contains("\"replicas\":3"));
    }

    @Test
    @Order(2)
    void testUpdateDeploymentWithYamlDataReturnJson() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateDeployment.yml");
      yamlString = yamlString.replace("pipelineUUIDPlaceholder", pipelineUuid.toString());

      RequestSpecification request =
          given().pathParam("uuid", deploymentUuid).config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_YAML).header(Utilities.ACCEPT_JSON);
      request.body(yamlString);
      Response response = request.put("{uuid}");

      DeploymentEntity de =
          jsonMapper.readValue(response.body().asString(), DeploymentEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(de);
    }

    @Test
    @Order(2)
    void testUpdateDeploymentWithJsonDataReturnYaml() throws IOException, URISyntaxException {
      String yamlString = Utilities.getStringFromFile("yamlTestFiles/updateDeployment.json");
      yamlString = yamlString.replace("pipelineUUIDPlaceholder", pipelineUuid.toString());

      RequestSpecification request =
          given().pathParam("uuid", deploymentUuid).config(Utilities.restAssuredConfig);

      request.header(Utilities.CONTENT_JSON).header(Utilities.ACCEPT_YAML);
      request.body(yamlString);
      Response response = request.put("{uuid}");

      DeploymentEntity de =
          yamlMapper.readValue(response.body().asString(), DeploymentEntity.class);

      Assertions.assertEquals(200, response.getStatusCode());
      Assertions.assertNotNull(de);
    }
  }
}

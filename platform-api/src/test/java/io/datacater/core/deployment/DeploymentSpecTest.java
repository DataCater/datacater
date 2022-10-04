package io.datacater.core.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeploymentSpecTest {
  Deployment deployment;

  @BeforeAll
  void setUp() throws IOException {
    URL errorLogs = ClassLoader.getSystemClassLoader().getResource("deployment-test-object.json");
    ObjectMapper mapper = new JsonMapper();
    deployment = mapper.readValue(errorLogs, Deployment.class);
  }

  @Test
  void testSerializeDeploymentSpec() throws JsonProcessingException {
    // arrange
    String expectedProp1Value = "prop1Value";
    String expectedProp2Value = "prop2Value";
    String expectedProp3Value = "prop3Value";
    DeploymentSpec spec = deployment.spec();
    JsonNode node = DeploymentSpec.serializeDeploymentSpec(spec.deployment());

    // act
    String prop1Value = node.findValue("prop1").asText();
    String prop2Value = node.findValue("prop2").asText();
    String prop3Value = node.findValue("prop3").asText();

    // assert
    Assertions.assertEquals(expectedProp1Value, prop1Value);
    Assertions.assertEquals(expectedProp2Value, prop2Value);
    Assertions.assertEquals(expectedProp3Value, prop3Value);
  }
}

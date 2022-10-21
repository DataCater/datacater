package io.datacater.core.deployment;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.TestInstance;

// TODO can probably safely remove this trash
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatacaterDeploymentSpecTest {
  //  DatacaterDeployment datacaterDeployment;
  //
  //  @BeforeAll
  //  void setUp() throws IOException {
  //    URL errorLogs =
  // ClassLoader.getSystemClassLoader().getResource("deployment-test-object.json");
  //    ObjectMapper mapper = new JsonMapper();
  //    datacaterDeployment = mapper.readValue(errorLogs, DatacaterDeployment.class);
  //  }
  //
  //  @Test
  //  void testSerializeDeploymentSpec() throws JsonProcessingException {
  //    // arrange
  //    String expectedProp1Value = "prop1Value";
  //    String expectedProp2Value = "prop2Value";
  //    String expectedProp3Value = "prop3Value";
  //    DeploymentSpec spec = datacaterDeployment.spec();
  //    JsonNode node = DeploymentSpec.serializeDeploymentSpec(spec.getDeployment());
  //
  //    // act
  //    String prop1Value = node.findValue("prop1").asText();
  //    String prop2Value = node.findValue("prop2").asText();
  //    String prop3Value = node.findValue("prop3").asText();
  //
  //    // assert
  //    Assertions.assertEquals(expectedProp1Value, prop1Value);
  //    Assertions.assertEquals(expectedProp2Value, prop2Value);
  //    Assertions.assertEquals(expectedProp3Value, prop3Value);
  //  }
}

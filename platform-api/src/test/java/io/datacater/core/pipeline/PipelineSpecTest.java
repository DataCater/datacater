package io.datacater.core.pipeline;

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
class PipelineSpecTest {

  Pipeline pipeline;

  @BeforeAll
  void setUp() throws IOException {
    URL errorLogs = ClassLoader.getSystemClassLoader().getResource("pipeline-test-object.json");
    ObjectMapper mapper = new JsonMapper();
    pipeline = mapper.readValue(errorLogs, Pipeline.class);
    JsonNode filters = mapper.readTree(mapper.writeValueAsString(pipeline.getSpec().filters));
    JsonNode transformationSteps =
        mapper.readTree(mapper.writeValueAsString(pipeline.getSpec().transformationSteps));
  }

  @Test
  void testSerializePipelineSpec() throws JsonProcessingException {
    // arrange
    String expectedFilterKey = "less-than";
    String expectedTransformationStepName = "First Step";
    PipelineSpec spec = pipeline.getSpec();
    JsonNode node =
        PipelineSpec.serializePipelineSpec(spec.getFilters(), spec.getTransformationSteps());

    // act
    JsonNode filters = node.findValue("filters");
    JsonNode transformationSteps = node.findValue("transformationSteps");
    String filterKey = filters.findValue("filter").asText();
    String transformationStepName = transformationSteps.findValue("name").asText();

    // assert
    Assertions.assertEquals(expectedFilterKey, filterKey);
    Assertions.assertEquals(expectedTransformationStepName, transformationStepName);
  }

  @Test
  void testSpecFromJsonNode() {}
}

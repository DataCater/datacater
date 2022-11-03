package io.datacater.core.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PipelineTest {
  Pipeline pipelineJSON;
  Pipeline pipelineYAML;

  @BeforeAll
  void setUp() throws IOException {
    URL pipelineJson = ClassLoader.getSystemClassLoader().getResource("pipeline-test-object.json");
    ObjectMapper mapper = new JsonMapper();
    pipelineJSON = mapper.readValue(pipelineJson, Pipeline.class);

    URL errorLogsYAML = ClassLoader.getSystemClassLoader().getResource("pipeline-error-logs.yaml");
    mapper = new YAMLMapper();
    pipelineYAML = mapper.readValue(errorLogsYAML, Pipeline.class);
  }

  @Test
  void testGetSerializedMetadata() throws JsonProcessingException {
    // arrange
    String expectedmetaProp1 = "1";
    String expectedmetaProp2 = "2";
    JsonNode node = pipelineJSON.getSerializedMetadata();

    // act
    String metaProp1 = node.findValue("metaProp1").asText();
    String metaProp2 = node.findValue("metaProp2").asText();

    // assert
    Assertions.assertEquals(expectedmetaProp1, metaProp1);
    Assertions.assertEquals(expectedmetaProp2, metaProp2);
  }

  @Test
  void testToString() {
    // arrange
    String expected =
        "---\nname: \"pipeline-error-logs\"\n"
            + "metadata:\n"
            + "  id: \"1\"\n"
            + "  name: \"DoK Demo Pipeline\"\n"
            + "spec:\n"
            + "  steps:\n"
            + "  - kind: \"Field\"\n"
            + "    fields:\n"
            + "      age:\n"
            + "        filter:\n"
            + "          key: \"less-than\"\n"
            + "          config:\n"
            + "            value: 50\n";

    // act
    String obj = pipelineYAML.toString();

    // assert
    Assertions.assertEquals(expected, obj);
  }
}

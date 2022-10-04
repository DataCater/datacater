package io.datacater.core.stream;

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
class StreamSpecTest {
  Stream stream;

  @BeforeAll
  void setUp() throws IOException {
    URL errorLogs =
        ClassLoader.getSystemClassLoader()
            .getResource("streamTestFiles/stream-test-json-format.json");
    ObjectMapper mapper = new JsonMapper();
    stream = mapper.readValue(errorLogs, Stream.class);
  }

  @Test
  void testSerializeStreamSpec() throws JsonProcessingException {
    // arrange
    String expectedBootstrapServers = "localhost:9092";
    String expectedKind = "KAFKA";
    StreamSpec spec = stream.spec();
    JsonNode node = spec.serializeStreamSpec();

    // act
    String actualBootstrapServers = node.findValue("bootstrap.servers").asText();
    String actualKind = node.findValue("kind").asText();

    // assert
    Assertions.assertEquals(expectedBootstrapServers, actualBootstrapServers);
    Assertions.assertEquals(expectedKind, actualKind);
  }
}

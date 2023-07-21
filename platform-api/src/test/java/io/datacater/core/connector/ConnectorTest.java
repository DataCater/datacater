package io.datacater.core.connector;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConnectorTest {
  private static final String testDeserializeNoConfig =
      """
  {
    "stream": "iAmAValidStream",
    "image": "some/image"
  }
  """;

  @Test
  void testDeserializationNoConfig() throws JsonProcessingException {
    ObjectMapper om = new ObjectMapper();
    Connector connector = om.readValue(testDeserializeNoConfig, Connector.class);
    Assertions.assertNotNull(connector.config());
  }
}

package io.datacater.core.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

public class ConnectorSpecTest {

  final String connectorPath = "connectorTests/connector.json";

  @Test
  void testDefaultDeserializer() throws IOException {
    URL jsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    JsonMapper jsonMapper = new JsonMapper();
    JsonNode root = jsonMapper.readTree(jsonURL);

    JsonNode spec = root.get("spec");
    Assert.assertTrue(spec.get("stream").getNodeType().equals(JsonNodeType.STRING));
  }

  @Test
  void testGetConnectorSpecDeser() throws IOException {
    URL jsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    JsonMapper jsonMapper = new JsonMapper();
    ConnectorSpec root = jsonMapper.readValue(jsonURL, ConnectorSpec.class);

    Assert.assertTrue(root.connector().stream().equals("streamUUIDPlaceholder"));
  }

  @Test
  void testGetConnectorMap() throws IOException {
    URL jsonURL = ClassLoader.getSystemClassLoader().getResource(connectorPath);
    JsonMapper jsonMapper = new JsonMapper();
    ConnectorSpec root = jsonMapper.readValue(jsonURL, ConnectorSpec.class);
    Map<String, Object> spec = (Map<String, Object>) root.getConnectorMap().get("spec");

    Assert.assertTrue(spec.containsKey("stream"));
  }
}

package io.datacater.core.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.exceptions.JsonNotParsableException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record ConnectorSpec(
    @JsonProperty(value = "name") @JsonSetter(nulls = Nulls.AS_EMPTY) String name,
    @Schema(
            example =
                """
            {
                "stream": "dc161a69-fa49-4b1a-b1b1-6d8246d50d72",
                "image": "datacater/concon-rss:latest"
              }
        """)
        @JsonProperty(value = "spec", required = true)
        Map<String, Object> connector,
    @JsonProperty(value = "configSelector") Map<String, String> configSelector) {
  public ConnectorSpec(String name, Map<String, Object> connector) {
    this(name, connector, new HashMap<>());
  }

  public static ConnectorSpec from(ConnectorSpec spec) {
    Map<String, Object> copiedMap = new HashMap<>();
    try {
      JsonNode specNode = ConnectorEntity.serializeMap(spec.connector());
      ObjectMapper mapper = new ObjectMapper();
      copiedMap = mapper.treeToValue(specNode, Map.class);
    } catch (JsonProcessingException e) {
      throw new JsonNotParsableException(e.getMessage());
    }

    return new ConnectorSpec(spec.name(), copiedMap, spec.configSelector());
  }
}

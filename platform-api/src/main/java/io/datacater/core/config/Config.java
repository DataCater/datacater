package io.datacater.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.config.enums.Kind;
import java.util.Map;

public record Config(String name, Kind kind, Map<String, Object> config) {
  @JsonCreator
  static Config from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "kind", required = true) Kind kind,
      @JsonProperty(value = "config", required = true) Map<String, Object> config) {
    return new Config(name, kind, config);
  }

  public JsonNode serializeConfigSpec() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(objectMapper.writeValueAsString(config));
  }
}

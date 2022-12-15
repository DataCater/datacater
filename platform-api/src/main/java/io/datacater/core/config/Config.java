package io.datacater.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.config.enums.Kind;
import java.util.Map;

public record Config(
    String name, Kind kind, Map<String, Object> metadata, Map<String, Object> spec) {
  @JsonCreator
  static Config from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "kind", required = true) Kind kind,
      @JsonProperty(value = "metadata", required = true) Map<String, Object> metadata,
      @JsonProperty(value = "spec", required = true) Map<String, Object> spec) {
    return new Config(name, kind, metadata, spec);
  }

  public JsonNode serializeMetadata() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(objectMapper.writeValueAsString(metadata));
  }

  public JsonNode serializeConfigSpec() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(objectMapper.writeValueAsString(spec));
  }
}

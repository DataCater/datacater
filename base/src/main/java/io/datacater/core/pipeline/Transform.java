package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Transform(String key, Map<String, Object> config) {
  @JsonCreator
  static Transform from(
      @JsonProperty(value = "key", required = true) String key,
      @JsonProperty(value = "config") Map<String, Object> config) {
    return new Transform(key, config);
  }

  static String serializeTransform(String key, Map<String, Object> config)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Transform serializedTransform = new Transform(key, config);
    return objectMapper.writeValueAsString(serializedTransform);
  }
}

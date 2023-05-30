package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Filter(String key, Map<String, Object> config) {
  @JsonCreator
  static Filter from(
      @JsonProperty(value = "key", required = true) String key,
      @JsonProperty(value = "config") Map<String, Object> config) {
    return new Filter(key, config);
  }

  static String serializeFilter(String key, Map<String, Object> config)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Filter serializedFilter = new Filter(key, config);
    return objectMapper.writeValueAsString(serializedFilter);
  }
}

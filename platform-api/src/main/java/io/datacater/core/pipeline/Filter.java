package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public record Filter(String attributeName, String filter, Map<String, Object> filterConfig) {
  @JsonCreator
  static Filter from(
      @JsonProperty(value = "attributeName", required = true) String attributeName,
      @JsonProperty(value = "filter", required = true) String filter,
      @JsonProperty(value = "filterConfig") Map<String, Object> filterConfig) {
    return new Filter(attributeName, filter, filterConfig);
  }

  static String serializeFilter(
      String attributeName, String filter, Map<String, Object> filterConfig)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Filter serializedFilter = new Filter(attributeName, filter, filterConfig);
    return objectMapper.writeValueAsString(serializedFilter);
  }
}

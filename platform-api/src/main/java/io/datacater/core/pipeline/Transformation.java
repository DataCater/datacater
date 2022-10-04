package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public record Transformation(
    String attributeName,
    String transformation,
    Map<String, Object> transformationConfig,
    String filter,
    Map<String, Object> filterConfig) {
  @JsonCreator
  public static Transformation from(
      @JsonProperty(value = "attributeName", required = true) String attributeName,
      @JsonProperty(value = "transformation", required = true) String transformation,
      @JsonProperty("transformationConfig") Map<String, Object> transformationConfig,
      @JsonProperty("filter") String filter,
      @JsonProperty("filterConfig") Map<String, Object> filterConfig) {
    return new Transformation(
        attributeName, transformation, transformationConfig, filter, filterConfig);
  }

  public static String serializeTransformation(
      String attributeName,
      String transformation,
      Map<String, Object> transformationConfig,
      String filter,
      Map<String, Object> filterConfig)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Transformation serializedTransformation =
        new Transformation(
            attributeName, transformation, transformationConfig, filter, filterConfig);
    return objectMapper.writeValueAsString(serializedTransformation);
  }
}

package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Step(
    String kind, String name, Map<String, Field> fields, Filter filter, Transform transform) {
  @JsonCreator
  public static Step from(
      @JsonProperty(value = "kind", required = true) String kind,
      @JsonProperty(value = "name") String name,
      @JsonProperty(value = "fields") Map<String, Field> fields,
      @JsonProperty(value = "filter") Filter filter,
      @JsonProperty(value = "transform") Transform transform) {
    return new Step(kind, name, fields, filter, transform);
  }

  public static String serializeStep(
      String kind, String name, Map<String, Field> fields, Filter filter, Transform transform)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Step serializedStep = new Step(kind, name, fields, filter, transform);
    return objectMapper.writeValueAsString(serializedStep);
  }
}

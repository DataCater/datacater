package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Field(Filter filter, Transform transform) {
  @JsonCreator
  public static Field from(
      @JsonProperty("filter") Filter filter, @JsonProperty("transform") Transform transform) {
    return new Field(filter, transform);
  }

  public static String serializeField(Filter filter, Transform transform)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Field serializedField = new Field(filter, transform);
    return objectMapper.writeValueAsString(serializedField);
  }
}

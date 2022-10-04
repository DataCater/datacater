package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record TransformationStep(String name, List<Transformation> transformations) {
  @JsonCreator
  public static TransformationStep from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty("transformations") List<Transformation> transformations) {
    return new TransformationStep(name, transformations);
  }

  public static String serializeTransformation(String name, List<Transformation> transformations)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    TransformationStep transformationStep = new TransformationStep(name, transformations);
    return objectMapper.writeValueAsString(transformationStep);
  }

  public List<Transformation> getTransformations() {
    return Optional.ofNullable(transformations).orElse(Collections.emptyList());
  }
}

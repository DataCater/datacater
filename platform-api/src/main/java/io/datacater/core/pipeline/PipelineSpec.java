package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.Embeddable;

@Embeddable
public class PipelineSpec {
  List<Filter> filters;
  List<TransformationStep> transformationSteps;

  @ExcludeFromGeneratedCoverageReport
  protected PipelineSpec() {}

  private PipelineSpec(List<Filter> filters, List<TransformationStep> transformationSteps) {
    this.transformationSteps = transformationSteps;
    this.filters = filters;
  }

  @JsonCreator
  static PipelineSpec from(
      @JsonProperty("filters") List<Filter> filters,
      @JsonProperty("transformationSteps") List<TransformationStep> transformationSteps) {
    return new PipelineSpec(filters, transformationSteps);
  }

  public static JsonNode serializePipelineSpec(
      List<Filter> filters, List<TransformationStep> transformationSteps)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    PipelineSpec ps = new PipelineSpec(filters, transformationSteps);
    return objectMapper.readTree(objectMapper.writeValueAsString(ps));
  }

  static PipelineSpec from(JsonNode json) throws JsonProcessingException {
    JsonMapper mapper = new JsonMapper();
    return mapper.treeToValue(json, PipelineSpec.class);
  }

  String asJson() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(this);
  }

  public List<Filter> getFilters() {
    return Optional.ofNullable(filters).orElse(Collections.emptyList());
  }

  public List<TransformationStep> getTransformationSteps() {
    return Optional.ofNullable(transformationSteps).orElse(Collections.emptyList());
  }
}

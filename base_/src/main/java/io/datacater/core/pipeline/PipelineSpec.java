package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;

import javax.persistence.Embeddable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Embeddable
public class PipelineSpec {
  List<Step> steps;

  @ExcludeFromGeneratedCoverageReport
  protected PipelineSpec() {}

  private PipelineSpec(List<Step> steps) {
    this.steps = steps;
  }

  @JsonCreator
  static PipelineSpec from(@JsonProperty("steps") List<Step> steps) {
    return new PipelineSpec(steps);
  }

  public static JsonNode serializePipelineSpec(List<Step> steps) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    PipelineSpec ps = new PipelineSpec(steps);
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

  public List<Step> getSteps() {
    return Optional.ofNullable(steps).orElse(Collections.emptyList());
  }
}

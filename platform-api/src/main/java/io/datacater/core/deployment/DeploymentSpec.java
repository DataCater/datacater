package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

public class DeploymentSpec {
  @JsonProperty("pipeline-in")
  private final String pipelineIn;

  @JsonProperty("pipeline-out")
  private final String pipelineOut;

  @JsonProperty("pipelineID")
  private final UUID pipelineId;

  private DeploymentSpec(String pipelineIn, String pipelineOut, UUID pipelineId) {
    this.pipelineIn = pipelineIn;
    this.pipelineOut = pipelineOut;
    this.pipelineId = pipelineId;
  }

  public static JsonNode serializeDeploymentSpec(
      String pipelineIn, String pipelineOut, UUID pipelineId) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    DeploymentSpec ds = new DeploymentSpec(pipelineIn, pipelineOut, pipelineId);
    return objectMapper.readTree(objectMapper.writeValueAsString(ds));
  }

  @JsonCreator
  static DeploymentSpec from(
      @JsonProperty("pipeline-in") String pipelineIn,
      @JsonProperty("pipeline-out") String pipelineOut,
      @JsonProperty("pipelineID") UUID pipelineId) {
    return new DeploymentSpec(pipelineIn, pipelineOut, pipelineId);
  }

  public String getPipelineIn() {
    return pipelineIn;
  }

  public String getPipelineOut() {
    return pipelineOut;
  }

  public UUID getPipelineId() {
    return pipelineId;
  }
}

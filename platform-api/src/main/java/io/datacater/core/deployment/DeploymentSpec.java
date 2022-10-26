package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

public class DeploymentSpec {
  @JsonProperty("stream-in")
  private final String streamIn;

  @JsonProperty("stream-out")
  private final String streamOut;

  @JsonProperty("pipelineID")
  private final UUID pipelineId;

  private DeploymentSpec(String streamIn, String streamOut, UUID pipelineId) {
    this.streamIn = streamIn;
    this.streamOut = streamOut;
    this.pipelineId = pipelineId;
  }

  public static JsonNode serializeDeploymentSpec(String streamIn, String streamOut, UUID pipelineId)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    DeploymentSpec ds = new DeploymentSpec(streamIn, streamOut, pipelineId);
    return objectMapper.readTree(objectMapper.writeValueAsString(ds));
  }

  @JsonCreator
  static DeploymentSpec from(
      @JsonProperty("stream-in") String streamIn,
      @JsonProperty("stream-out") String streamOut,
      @JsonProperty("pipelineID") UUID pipelineId) {
    return new DeploymentSpec(streamIn, streamOut, pipelineId);
  }

  public String getStreamIn() {
    return streamIn;
  }

  public String getStreamOut() {
    return streamOut;
  }

  public UUID getPipelineId() {
    return pipelineId;
  }
}

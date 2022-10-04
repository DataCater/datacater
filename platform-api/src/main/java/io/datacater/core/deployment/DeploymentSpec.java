package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public record DeploymentSpec(Map<String, String> deployment) {
  public static JsonNode serializeDeploymentSpec(Map<String, String> deployment)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    DeploymentSpec ds = new DeploymentSpec(deployment);
    return objectMapper.readTree(objectMapper.writeValueAsString(ds));
  }

  @JsonCreator
  static DeploymentSpec from(@JsonProperty("deployment") Map<String, String> deployment) {
    return new DeploymentSpec(deployment);
  }
}

package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record DeploymentSpec(
    @Schema(
            example =
                """
            {
            "pipeline": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
          }
        """)
        Map<String, String> deployment) {
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

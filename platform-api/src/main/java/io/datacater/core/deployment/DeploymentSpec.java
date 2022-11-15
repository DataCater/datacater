package io.datacater.core.deployment;

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
                "pipeline": "dc161a69-fa49-4b1a-b1b1-6d8246d50d72",
                "stream-in-config": {
                  "bootstrap.servers": "localhost:9092"
                },
                "stream-out-config": {
                  "bootstrap.servers": "localhost:9092"
                }
              }
        """)
        @JsonProperty(value = "spec", required = true)
        Map<String, Object> deployment) {

  public static JsonNode serializeDeploymentSpec(Map<String, Object> deployment)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    DeploymentSpec ds = new DeploymentSpec(deployment);
    return objectMapper.readTree(objectMapper.writeValueAsString(ds));
  }
}

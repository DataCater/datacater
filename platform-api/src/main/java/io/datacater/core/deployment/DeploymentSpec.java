package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record DeploymentSpec(
    @JsonProperty(value = "name") @JsonSetter(nulls = Nulls.AS_EMPTY) String name,
    @Schema(
            example =
                """
            {
                "pipeline": "dc161a69-fa49-4b1a-b1b1-6d8246d50d72",
                "stream-in-config": {
                  "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                },
                "stream-out-config": {
                  "uuid": "49b079f8-c685-43ac-8419-986060573045"
                }
              }
        """)
        @JsonProperty(value = "spec", required = true)
        Map<String, Object> deployment,
    Map<String, String> labels) {
  public DeploymentSpec(String name, Map<String, Object> deployment) {
    this(name, deployment, new HashMap<>());
  }
}

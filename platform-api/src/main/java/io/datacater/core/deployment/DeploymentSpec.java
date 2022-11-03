package io.datacater.core.deployment;

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
        Map<String, Object> deployment) {}

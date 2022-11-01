package io.datacater.core.deployment;

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
        Map<String, String> deployment) {}

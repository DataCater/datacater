package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Deployment(DeploymentSpec spec) {
  @JsonCreator
  static Deployment from(@JsonProperty(value = "spec", required = true) DeploymentSpec spec) {
    return new Deployment(spec);
  }
}

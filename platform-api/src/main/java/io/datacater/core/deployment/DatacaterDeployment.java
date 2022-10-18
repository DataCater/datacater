package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DatacaterDeployment(String name, DeploymentSpec spec) {
  @JsonCreator
  static DatacaterDeployment from(
      @JsonProperty(value = "name") String name,
      @JsonProperty(value = "spec", required = true) DeploymentSpec spec) {
    return new DatacaterDeployment(name, spec);
  }
}

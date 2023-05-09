package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceInfo(
    @JsonProperty("streams") ResourceItem streams,
    @JsonProperty("deployments") ResourceItem deployments,
    @JsonProperty("pipelines") ResourceItem pipelines,
    @JsonProperty("configs") ResourceItem configs) {

  @JsonCreator
  static ResourceInfo from(
      @JsonProperty(value = "streams") ResourceItem streams,
      @JsonProperty(value = "deployments") ResourceItem deployments,
      @JsonProperty(value = "pipelines") ResourceItem pipelines,
      @JsonProperty(value = "configs") ResourceItem configs) {
    return new ResourceInfo(streams, deployments, pipelines, configs);
  }
}

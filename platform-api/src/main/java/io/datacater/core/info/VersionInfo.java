package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VersionInfo(
    @JsonProperty("version") String version,
    @JsonProperty("base-image") String baseImage,
    @JsonProperty("pipeline-image") String pipelineImage,
    @JsonProperty("python-runner-image") String pythonRunnerImage) {

  @JsonCreator
  static VersionInfo from(
      @JsonProperty(value = "version") String version,
      @JsonProperty(value = "base-image") String baseImage,
      @JsonProperty(value = "pipeline-image") String pipelineImage,
      @JsonProperty(value = "python-runner-image") String pythonRunnerImage) {
    return new VersionInfo(version, baseImage, pipelineImage, pythonRunnerImage);
  }
}

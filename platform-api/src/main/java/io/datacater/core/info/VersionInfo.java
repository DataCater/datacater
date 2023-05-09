package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VersionInfo(
    @JsonProperty("version") String version,
    @JsonProperty("buildDate") String buildDate,
    @JsonProperty("platform") String platform,
    @JsonProperty("gitCommitHash") String gitCommitHash,
    @JsonProperty("baseImage") String baseImage,
    @JsonProperty("pipelineImage") String pipelineImage,
    @JsonProperty("pythonRunnerImage") String pythonRunnerImage) {

  @JsonCreator
  static VersionInfo from(
      @JsonProperty(value = "version") String version,
      @JsonProperty(value = "buildDate") String buildDate,
      @JsonProperty(value = "platform") String platform,
      @JsonProperty(value = "gitCommitHash") String gitCommitHash,
      @JsonProperty(value = "baseImage") String baseImage,
      @JsonProperty(value = "pipelineImage") String pipelineImage,
      @JsonProperty(value = "pythonRunnerImage") String pythonRunnerImage) {
    return new VersionInfo(
        version, buildDate, platform, gitCommitHash, baseImage, pipelineImage, pythonRunnerImage);
  }
}

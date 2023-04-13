package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VersionInfo(
    @JsonProperty("version") String version,
    @JsonProperty("build-date") String buildDate,
    @JsonProperty("platform") String platform,
    @JsonProperty("git-commit-hash") String gitCommitHash,
    @JsonProperty("base-image") String baseImage,
    @JsonProperty("pipeline-image") String pipelineImage,
    @JsonProperty("python-runner-image") String pythonRunnerImage) {

  @JsonCreator
  static VersionInfo from(
      @JsonProperty(value = "version") String version,
      @JsonProperty(value = "build-date") String buildDate,
      @JsonProperty(value = "platform") String platform,
      @JsonProperty(value = "git-commit-hash") String gitCommitHash,
      @JsonProperty(value = "base-image") String baseImage,
      @JsonProperty(value = "pipeline-image") String pipelineImage,
      @JsonProperty(value = "python-runner-image") String pythonRunnerImage) {
    return new VersionInfo(
        version, buildDate, platform, gitCommitHash, baseImage, pipelineImage, pythonRunnerImage);
  }
}

package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VersionInfo(
    @JsonProperty("version") String version,
    @JsonProperty("build-date") String buildDate,
    @JsonProperty("platform") String platform,
    @JsonProperty("git-commit-hash") String gitCommitHash) {

  @JsonCreator
  static VersionInfo from(
      @JsonProperty(value = "version") String version,
      @JsonProperty(value = "build-date") String buildDate,
      @JsonProperty(value = "platform") String platform,
      @JsonProperty(value = "git-commit-hash") String gitCommitHash) {
    return new VersionInfo(version, buildDate, platform, gitCommitHash);
  }
}

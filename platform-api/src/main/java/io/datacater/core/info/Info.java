package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Info(
    @JsonProperty("version") VersionInfo version,
    @JsonProperty("resources") ResourceInfo resources,
    @JsonProperty("contact") ContactInfo contact) {
  @JsonCreator
  static Info from(
      @JsonProperty(value = "version") VersionInfo version,
      @JsonProperty(value = "resources") ResourceInfo resources,
      @JsonProperty(value = "contact") ContactInfo contact) {
    return new Info(version, resources, contact);
  }

  public Info() {
    this(
        VersionInfo.from(StaticInformation.EnvironmentVariables.VERSION, StaticInformation.EnvironmentVariables.BUILD_DATE, StaticInformation.EnvironmentVariables.PLATFORM, StaticInformation.EnvironmentVariables.GIT_COMMIT_VERSION),
        ResourceInfo.from(
            ResourceItem.from("/" + StaticInformation.STREAMS_TEXT, StaticInformation.API_DOCUMENTATION_PATH_PREFIX + StaticInformation.STREAMS_TEXT),
            ResourceItem.from("/" + StaticInformation.DEPLOYMENTS_TEXT, StaticInformation.API_DOCUMENTATION_PATH_PREFIX + StaticInformation.DEPLOYMENTS_TEXT),
            ResourceItem.from("/" + StaticInformation.PIPELINES_TEXT, StaticInformation.API_DOCUMENTATION_PATH_PREFIX + StaticInformation.PIPELINES_TEXT),
            ResourceItem.from("/" + StaticInformation.CONFIGS_TEXT, StaticInformation.API_DOCUMENTATION_PATH_PREFIX + StaticInformation.CONFIGS_TEXT)),
        ContactInfo.from(StaticInformation.EnvironmentVariables.CONTACT_NAME, StaticInformation.EnvironmentVariables.CONTACT_EMAIL, StaticInformation.EnvironmentVariables.CONTACT_URL));
  }
}

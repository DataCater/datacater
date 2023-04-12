package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Info(
    @JsonProperty("version") VersionInfo version,
    @JsonProperty("resources") ResourceInfo resources,
    @JsonProperty("contact") ContactInfo contact) {
  public Info(String path) {
    this(
        VersionInfo.from(
            StaticInformation.EnvironmentVariables.VERSION,
            StaticInformation.EnvironmentVariables.BASE_IMAGE
                + StaticInformation.EnvironmentVariables.VERSION,
            StaticInformation.EnvironmentVariables.PIPELINE_IMAGE,
            StaticInformation.EnvironmentVariables.PYTHON_RUNNER_IMAGE
                + ":"
                + StaticInformation.EnvironmentVariables.PYTHON_RUNNER_IMAGE_VERSION),
        ResourceInfo.from(
            ResourceItem.from(
                path + StaticInformation.STREAMS_SUFFIX,
                StaticInformation.API_DOCUMENTATION_PATH_PREFIX + StaticInformation.STREAMS_SUFFIX),
            ResourceItem.from(
                path + StaticInformation.DEPLOYMENTS_SUFFIX,
                StaticInformation.API_DOCUMENTATION_PATH_PREFIX
                    + StaticInformation.DEPLOYMENTS_SUFFIX),
            ResourceItem.from(
                path + StaticInformation.PIPELINES_SUFFIX,
                StaticInformation.API_DOCUMENTATION_PATH_PREFIX
                    + StaticInformation.PIPELINES_SUFFIX),
            ResourceItem.from(
                path + StaticInformation.CONFIGS_SUFFIX,
                StaticInformation.API_DOCUMENTATION_PATH_PREFIX
                    + StaticInformation.CONFIGS_SUFFIX)),
        ContactInfo.from(
            StaticInformation.EnvironmentVariables.CONTACT_NAME,
            StaticInformation.EnvironmentVariables.CONTACT_EMAIL,
            StaticInformation.EnvironmentVariables.CONTACT_URL));
  }
}

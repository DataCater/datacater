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
            StaticInformation.EnvironmentVariables.BUILD_DATE,
            System.getProperty(StaticInformation.SYSTEM_PROPERTY_OS_ARCH_TEXT),
            StaticInformation.EnvironmentVariables.GIT_COMMIT_VERSION,
            StaticInformation.EnvironmentVariables.BASE_IMAGE,
            StaticInformation.EnvironmentVariables.PIPELINE_IMAGE,
            StaticInformation.EnvironmentVariables.PYTHON_RUNNER_IMAGE
                + ":"
                + StaticInformation.EnvironmentVariables.PYTHON_RUNNER_IMAGE_VERSION),
        ResourceInfo.from(
            ResourceItem.from(
                StaticInformation.mapUrlAndResource(path, StaticInformation.STREAMS_SUFFIX),
                StaticInformation.mapUrlAndResource(
                    StaticInformation.API_DOCUMENTATION_PATH_PREFIX,
                    StaticInformation.STREAMS_SUFFIX)),
            ResourceItem.from(
                StaticInformation.mapUrlAndResource(path, StaticInformation.DEPLOYMENTS_SUFFIX),
                StaticInformation.mapUrlAndResource(
                    StaticInformation.API_DOCUMENTATION_PATH_PREFIX,
                    StaticInformation.DEPLOYMENTS_SUFFIX)),
            ResourceItem.from(
                StaticInformation.mapUrlAndResource(path, StaticInformation.PIPELINES_SUFFIX),
                StaticInformation.mapUrlAndResource(
                    StaticInformation.API_DOCUMENTATION_PATH_PREFIX,
                    StaticInformation.PIPELINES_SUFFIX)),
            ResourceItem.from(
                StaticInformation.mapUrlAndResource(path, StaticInformation.CONFIGS_SUFFIX),
                StaticInformation.mapUrlAndResource(
                    StaticInformation.API_DOCUMENTATION_PATH_PREFIX,
                    StaticInformation.CONFIGS_SUFFIX))),
        ContactInfo.from(
            StaticInformation.EnvironmentVariables.CONTACT_NAME,
            StaticInformation.EnvironmentVariables.CONTACT_EMAIL,
            StaticInformation.EnvironmentVariables.CONTACT_URL));
  }
}

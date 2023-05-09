package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceItem(
    @JsonProperty("url") String url, @JsonProperty("documentationUrl") String documentationUrl) {

  @JsonCreator
  static ResourceItem from(
      @JsonProperty(value = "url") String url,
      @JsonProperty(value = "documentationUrl") String documentationUrl) {
    return new ResourceItem(url, documentationUrl);
  }
}

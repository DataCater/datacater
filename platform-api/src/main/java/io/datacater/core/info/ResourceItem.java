package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceItem(
    @JsonProperty("url") String url, @JsonProperty("documentation-url") String documentationLink) {

  @JsonCreator
  static ResourceItem from(
      @JsonProperty(value = "url") String url,
      @JsonProperty(value = "documentation-url") String documentationLink) {
    return new ResourceItem(url, documentationLink);
  }
}

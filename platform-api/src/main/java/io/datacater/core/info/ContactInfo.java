package io.datacater.core.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ContactInfo(
    @JsonProperty("name") String name,
    @JsonProperty("email") String email,
    @JsonProperty("url") String url) {
  @JsonCreator
  static ContactInfo from(
      @JsonProperty(value = "name") String name,
      @JsonProperty(value = "email") String email,
      @JsonProperty(value = "url") String url) {
    return new ContactInfo(name, email, url);
  }
}

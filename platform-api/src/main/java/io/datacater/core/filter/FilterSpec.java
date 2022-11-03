package io.datacater.core.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FilterSpec(
    String kind,
    String name,
    String key,
    String description,
    String license,
    String website,
    Author author,
    Map<String, String[]> labels,
    List<Map<String, Object>> config,
    String version) {
  @JsonCreator
  static FilterSpec from(
      @JsonProperty(value = "kind", required = true) String kind,
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "key", required = true) String key,
      @JsonProperty(value = "description") String description,
      @JsonProperty(value = "license", required = true) String license,
      @JsonProperty(value = "website") String website,
      @JsonProperty(value = "author") Author author,
      @JsonProperty(value = "labels") Map<String, String[]> labels,
      @JsonProperty(value = "config") List<Map<String, Object>> config,
      @JsonProperty(value = "version", required = true) String version) {
    return new FilterSpec(
        kind, name, key, description, license, website, author, labels, config, version);
  }
}

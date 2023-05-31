package io.datacater.core.connector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {
  @JsonProperty("name")
  String name;

  @JsonProperty("tag")
  String tag;

  private Image(String name, String tag) {
    this.name = name;
    this.tag = tag;
  }

  @JsonCreator
  public static Image from(@JsonProperty("name") String name, @JsonProperty("tag") String tag) {
    return new Image(name, tag);
  }
}

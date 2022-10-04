package io.datacater.core.stream;

public enum Kind {
  KAFKA("kafka");

  private String kind;

  Kind(String kind) {
    this.kind = kind;
  }
}

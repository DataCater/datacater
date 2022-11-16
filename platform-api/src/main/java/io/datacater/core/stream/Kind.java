package io.datacater.core.stream;

public enum Kind {
  KAFKA("kafka");

  private final String kind;

  Kind(String kind) {
    this.kind = kind;
  }
}

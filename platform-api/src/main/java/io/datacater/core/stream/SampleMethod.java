package io.datacater.core.stream;

public enum SampleMethod {
  UNIFORM("uniform"),
  SEQUENCED("sequenced");

  private final String method;

  SampleMethod(String method) {
    this.method = method;
  }
}

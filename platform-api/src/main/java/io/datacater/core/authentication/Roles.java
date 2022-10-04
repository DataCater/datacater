package io.datacater.core.authentication;

public enum Roles {
  DEV("dev"),
  USER("user"),
  ADMIN("admin");

  final String role;

  Roles(String role) {
    this.role = role;
  }
}

package io.datacater.core.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Token(
    @JsonProperty(value = "token_type") String tokenType,
    @JsonProperty(value = "access_token") String accessToken,
    @JsonProperty(value = "expires_in") int expiresIn) {
  @JsonCreator
  static Token from(
      @JsonProperty(value = "token_type", required = true) String tokenType,
      @JsonProperty(value = "access_token", required = true) String accessToken,
      @JsonProperty(value = "expires_in", required = true) int expiresIn) {
    return new Token(tokenType, accessToken, expiresIn);
  }
}

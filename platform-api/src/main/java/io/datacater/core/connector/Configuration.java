package io.datacater.core.connector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Configuration(
    String name, String displayName, String description, String defaultValue, String type) {}

package io.datacater.core.connector;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Map;

@JsonSerialize
public record ConnectorType(
    String image,
    String name,
    String version,
    Map<String, String> predefinedConfiguration,
    List<Configuration> configuration) {}

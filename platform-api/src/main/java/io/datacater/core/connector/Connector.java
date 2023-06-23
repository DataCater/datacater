package io.datacater.core.connector;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;

@JsonSerialize
public record Connector(String stream, String image, Map<String, Object> config) {}

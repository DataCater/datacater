package io.datacater.core.connector;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;

@JsonSerialize
public record Connector(
    @JsonSetter(nulls = Nulls.AS_EMPTY) String stream,
    @JsonSetter(nulls = Nulls.AS_EMPTY) String image,
    @JsonSetter(nulls = Nulls.AS_EMPTY) Map<String, Object> config) {}

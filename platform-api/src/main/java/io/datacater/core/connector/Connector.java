package io.datacater.core.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Map;

public record Connector(
    @JsonProperty(value = "stream") @JsonSetter(nulls = Nulls.AS_EMPTY) String stream,
    @JsonProperty(value = "image") @JsonSetter(nulls = Nulls.AS_EMPTY) String image,
    @JsonProperty(value = "config") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, Object> config) {}

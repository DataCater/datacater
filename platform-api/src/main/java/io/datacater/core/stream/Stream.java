package io.datacater.core.stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.utilities.JsonUtilities;
import java.util.Map;

public record Stream(String name, StreamSpec spec, Map<String, String> configSelector) {
  @JsonCreator
  static Stream from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "spec", required = true) StreamSpec spec,
      @JsonProperty(value = "configSelector") Map<String, String> configSelector) {
    return new Stream(name, spec, configSelector);
  }

  public static Stream from(StreamEntity se) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    StreamSpec spec = mapper.treeToValue(se.getSpec(), StreamSpec.class);
    return new Stream(se.getName(), spec, JsonUtilities.toStringMap(se.getConfigSelector()));
  }
}

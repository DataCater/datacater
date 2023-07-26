package io.datacater.core.stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.utilities.JsonUtilities;
import java.util.Map;

public record Stream(
    String name,
    StreamSpec spec,
    Map<String, String> configSelector,
    Map<String, String> projectSelector) {
  @JsonCreator
  static Stream from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "spec", required = true) StreamSpec spec,
      @JsonProperty(value = "configSelector") Map<String, String> configSelector,
      @JsonProperty(value = "projectSelector") Map<String, String> projectSelector) {
    return new Stream(name, spec, configSelector, projectSelector);
  }

  public static Stream from(StreamEntity se) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    StreamSpec spec = mapper.treeToValue(se.getSpec(), StreamSpec.class);
    return new Stream(
        se.getName(),
        spec,
        JsonUtilities.toStringMap(se.getConfigSelector()),
        JsonUtilities.toStringMap(se.getProjectSelector()));
  }

  public static Stream from(Stream stream) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    StreamSpec copiedSpec =
        mapper.treeToValue(stream.spec().serializeStreamSpec(), StreamSpec.class);
    return new Stream(stream.name(), copiedSpec, stream.configSelector(), stream.projectSelector());
  }
}

package io.datacater.core.stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.utilities.JsonUtilities;
import java.util.List;
import java.util.Map;

public record Stream(String name, StreamSpec spec, Map<String, List<String>> labels) {
  @JsonCreator
  static Stream from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "spec", required = true) StreamSpec spec,
      @JsonProperty(value = "labels", required = true) Map<String, List<String>> labels) {
    return new Stream(name, spec, labels);
  }

  public static Stream from(StreamEntity se) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    StreamSpec spec = mapper.treeToValue(se.getSpec(), StreamSpec.class);
    return new Stream(se.getName(), spec, JsonUtilities.toStringMap(se.getLabels()));
  }
}

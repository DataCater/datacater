package io.datacater.core.stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

public record StreamMessage(
    Map<String, Object> key, Map<String, Object> value, Map<String, Object> metadata) {

  @JsonCreator
  static StreamMessage from(
      @JsonProperty(value = "key") Map<String, Object> key,
      @JsonProperty(value = "value", required = true) Map<String, Object> value,
      @JsonProperty(value = "metadata", required = true) Map<String, Object> metadata) {
    return new StreamMessage(key, value, metadata);
  }

  public String toRecordJsonString() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.createObjectNode();
    ObjectNode all = rootNode.putPOJO("record", this);
    return mapper.writeValueAsString(all);
  }
}

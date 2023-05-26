package io.datacater.core.stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Collections;
import java.util.Map;

public class StreamSpec {

  @Schema(
      example =
          """
      {
      "bootstrap.servers": "",
      "value.serializer": "",
      "topic": {
        "num.partitions": "",
        "replication.factor": "",
        "config": {}
      }
    }
          """)
  private Map<String, Object> kafka;

  private Kind kind;

  public JsonNode serializeStreamSpec() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    StreamSpec ss = new StreamSpec(this.kafka, this.kind);
    return objectMapper.readTree(objectMapper.writeValueAsString(ss));
  }

  @JsonCreator
  static StreamSpec from(
      @JsonProperty("kafka") Map<String, Object> kafka, @JsonProperty("kind") Kind kind) {
    return new StreamSpec(kafka, kind);
  }

  private StreamSpec(Map<String, Object> kafka, Kind kind) {
    this.kafka = kafka;
    this.kind = kind;
  }

  public Map<String, Object> getKafka() {
    return kafka;
  }

  public Kind getKind() {
    return kind;
  }

  @JsonIgnore
  public Map<String, Object> getTopic() {
    return (Map<String, Object>) kafka.getOrDefault("topic", Collections.emptyMap());
  }

  @JsonIgnore
  public Map<String, String> getConfig() {
    return (Map<String, String>) getTopic().getOrDefault("config", Collections.emptyMap());
  }
}

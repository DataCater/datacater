package io.datacater.core.stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.datacater.core.utilities.JsonUtilities;
import io.quarkiverse.hibernate.types.json.JsonBinaryType;
import io.quarkiverse.hibernate.types.json.JsonType;
import io.quarkiverse.hibernate.types.json.JsonTypes;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

@TypeDef(name = JsonTypes.JSON, typeClass = JsonType.class)
@TypeDef(name = JsonTypes.JSON_BIN, typeClass = JsonBinaryType.class)
@Table(name = "streams")
@Entity
public class StreamEntity {
  @Id
  @GeneratedValue
  @JsonProperty("uuid")
  private UUID id;

  @CreationTimestamp
  @JsonProperty("createdAt")
  private Date createdAt;

  @UpdateTimestamp
  @JsonProperty("updatedAt")
  private Date updatedAt;

  @JsonProperty("name")
  private String name;

  @Type(type = JsonTypes.JSON)
  @Column(name = "spec", columnDefinition = JsonTypes.JSON_BIN)
  @JsonProperty("spec")
  private JsonNode spec;

  @Type(type = JsonTypes.JSON)
  @Column(name = "labels", columnDefinition = JsonTypes.JSON_BIN)
  @JsonProperty("labels")
  private JsonNode labels;

  protected StreamEntity() {}

  public StreamEntity(String name, StreamSpec spec) throws JsonProcessingException {
    this.name = name;
    this.spec = spec.serializeStreamSpec();
    this.labels = JsonNodeFactory.instance.objectNode();
  }

  public StreamEntity(String name, StreamSpec spec, Map<String, List<String>> labels)
      throws JsonProcessingException {
    this.name = name;
    this.spec = spec.serializeStreamSpec();
    this.labels = JsonUtilities.convertMap(labels);
  }

  public StreamEntity updateEntity(Stream stream) throws JsonProcessingException {
    this.spec = stream.spec().serializeStreamSpec();
    return this;
  }

  public String getName() {
    return name;
  }

  public JsonNode getSpec() {
    return spec;
  }

  public UUID getId() {
    return id;
  }

  public JsonNode getLabels() {
    return labels;
  }
}

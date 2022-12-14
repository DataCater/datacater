package io.datacater.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.core.config.enums.Kind;
import io.datacater.core.exceptions.JsonNotParsableException;
import io.quarkiverse.hibernate.types.json.JsonBinaryType;
import io.quarkiverse.hibernate.types.json.JsonTypes;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

@TypeDef(name = JsonTypes.JSON_BIN, typeClass = JsonBinaryType.class)
@Table(name = "configs")
@Entity
public class ConfigEntity {
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

  @JsonProperty("kind")
  private String kind;

  @Type(type = JsonTypes.JSON)
  @JsonProperty("metadata")
  @Column(name = "metadata", columnDefinition = JsonTypes.JSON_BIN)
  private JsonNode metadata;

  @Type(type = JsonTypes.JSON)
  @JsonProperty("spec")
  @Column(name = "spec", columnDefinition = JsonTypes.JSON_BIN)
  private JsonNode spec;

  protected ConfigEntity() {}

  protected ConfigEntity(String name, Kind kind, JsonNode metadata, JsonNode spec) {
    this.name = name;
    this.kind = kind.toString();
    this.metadata = metadata;
    this.spec = spec;
  }

  public ConfigEntity updateEntity(Config config) {
    this.name = config.name();
    this.kind = config.kind().toString();

    try {
      this.spec = config.serializeConfigSpec();
      this.metadata = config.serializeMetadata();
    } catch (JsonProcessingException e) {
      throw new JsonNotParsableException(e.getMessage());
    }

    return this;
  }

  public static ConfigEntity from(
      String name, Kind kind, Map<String, Object> metadata, Map<String, Object> spec)
      throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode specJson = objectMapper.readTree(objectMapper.writeValueAsString(spec));
    JsonNode metadataJson = objectMapper.readTree(objectMapper.writeValueAsString(metadata));

    return new ConfigEntity(name, kind, metadataJson, specJson);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Kind getKind() {
    return Kind.valueOf(this.kind);
  }

  public JsonNode getMetadata() {
    return metadata;
  }

  public JsonNode getSpec() {
    return spec;
  }
}

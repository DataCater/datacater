package io.datacater.core.stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.tenantAwareness.AbstractBaseEntity;
import io.quarkiverse.hibernate.types.json.JsonBinaryType;
import io.quarkiverse.hibernate.types.json.JsonType;
import io.quarkiverse.hibernate.types.json.JsonTypes;
import java.util.Date;
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
public class StreamEntity extends AbstractBaseEntity {
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

  protected StreamEntity() {}

  public StreamEntity(String name, StreamSpec spec) throws JsonProcessingException {
    this.name = name;
    this.spec = spec.serializeStreamSpec();
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
}

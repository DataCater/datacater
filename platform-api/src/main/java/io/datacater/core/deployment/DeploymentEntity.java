package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.hibernate.types.json.JsonBinaryType;
import io.quarkiverse.hibernate.types.json.JsonType;
import io.quarkiverse.hibernate.types.json.JsonTypes;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

@TypeDef(name = JsonTypes.JSON, typeClass = JsonType.class)
@TypeDef(name = JsonTypes.JSON_BIN, typeClass = JsonBinaryType.class)
@Table(name = "deployments")
@Entity
public class DeploymentEntity {
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

  @JsonProperty("status")
  @Transient
  private JsonNode status;

  protected DeploymentEntity() {}

  public static JsonNode serializeMap(Map<String, Object> map) {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.valueToTree(map);
  }

  public DeploymentEntity(DeploymentSpec spec) {
    this.name = spec.name();
    this.spec = DeploymentEntity.serializeMap(spec.deployment());
  }

  protected void setSpec(JsonNode spec) {
    this.spec = spec;
  }

  protected void setStatus(JsonNode status) {
    this.status = status;
  }

  protected UUID getId() {
    return this.id;
  }

  protected JsonNode getSpec() {
    return this.spec;
  }
}

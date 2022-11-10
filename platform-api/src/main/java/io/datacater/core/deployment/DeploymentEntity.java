package io.datacater.core.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
@Table(name = "deployments")
@Entity
public class DeploymentEntity {
  @Id @GeneratedValue private UUID id;
  @CreationTimestamp private Date createdAt;
  @UpdateTimestamp private Date updatedAt;

  @Type(type = JsonTypes.JSON)
  @Column(name = "spec", columnDefinition = JsonTypes.JSON_BIN)
  private JsonNode spec;

  protected DeploymentEntity() {}

  public DeploymentEntity(DeploymentSpec spec) throws JsonProcessingException {
    this.spec = DeploymentSpec.serializeDeploymentSpec(spec.deployment());
  }

  protected void setSpec(JsonNode spec) {
    this.spec = spec;
  }

  protected UUID getId() {
    return this.id;
  }

  protected JsonNode getSpec() {
    return this.spec;
  }
}

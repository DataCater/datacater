package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.exceptions.JsonNotParsableException;
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

  protected DeploymentEntity() {}

  public DeploymentEntity(DeploymentSpec spec) throws JsonProcessingException {
    this.spec =
        DeploymentSpec.serializeDeploymentSpec(
            spec.getPipelineIn(), spec.getPipelineOut(), spec.getPipelineId());
  }

  public DeploymentEntity(DatacaterDeployment datacaterDeployment) throws JsonProcessingException {
    this.name = datacaterDeployment.name();
    this.spec =
        DeploymentSpec.serializeDeploymentSpec(
            datacaterDeployment.spec().getPipelineIn(),
            datacaterDeployment.spec().getPipelineOut(),
            datacaterDeployment.spec().getPipelineId());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSpec(DeploymentSpec spec) {
    try {
      this.spec =
          DeploymentSpec.serializeDeploymentSpec(
              spec.getPipelineIn(), spec.getPipelineOut(), spec.getPipelineId());
    } catch (JsonProcessingException e) {
      throw new JsonNotParsableException(e.getMessage());
    }
  }

  public UUID getId() {
    return id;
  }
}

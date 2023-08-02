package io.datacater.core.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.datacater.core.exceptions.JsonNotParsableException;
import io.datacater.core.utilities.JsonUtilities;
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

@Entity
@Table(name = "pipeline")
@TypeDef(name = JsonTypes.JSON, typeClass = JsonType.class)
@TypeDef(name = JsonTypes.JSON_BIN, typeClass = JsonBinaryType.class)
public class ProjectEntity {
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

  public ProjectEntity() {}

  private ProjectEntity(String name, JsonNode spec) {
    this.name = name;
    this.spec = spec;
  }

  @JsonIgnore
  public static ProjectEntity from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "spec", required = true) JsonNode spec) {
    return new ProjectEntity(name, spec);
  }

  @JsonIgnore
  public ProjectEntity updateEntity(Project project) {
    this.name = project.getName();
    this.spec = JsonUtilities.convertStringMap(project.getSpec());
    return this;
  }

  public UUID getId() {
    return id;
  }

  public JsonNode getSpec() {
    return spec;
  }

  public String getName() {
    return name;
  }

  @ExcludeFromGeneratedCoverageReport
  public String asJsonString() {
    try {
      ObjectMapper mapper = new JsonMapper();
      mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonNotParsableException(e.getMessage());
    }
  }
}

package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "pipeline")
@TypeDef(name = JsonTypes.JSON, typeClass = JsonType.class)
@TypeDef(name = JsonTypes.JSON_BIN, typeClass = JsonBinaryType.class)
public class PipelineEntity {
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
  @Column(name = "metadata", columnDefinition = JsonTypes.JSON_BIN)
  @JsonProperty("metadata")
  private JsonNode metadata;

  @Type(type = JsonTypes.JSON)
  @Column(name = "spec", columnDefinition = JsonTypes.JSON_BIN)
  @JsonProperty("spec")
  private JsonNode spec;

  @Type(type = JsonTypes.JSON)
  @Column(name = "projectSelector", columnDefinition = JsonTypes.JSON_BIN)
  @JsonProperty("projectSelector")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode projectSelector;

  public PipelineEntity() {}

  private PipelineEntity(String name, JsonNode metadata, JsonNode spec, JsonNode projectSelector) {
    this.name = name;
    this.metadata = metadata;
    this.spec = spec;
    this.projectSelector = projectSelector;
  }

  @JsonIgnore
  public static PipelineEntity from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "metadata", required = true) JsonNode metadata,
      @JsonProperty(value = "spec", required = true) JsonNode spec,
      @JsonProperty(value = "projectSelector", required = true) JsonNode projectSelector) {
    return new PipelineEntity(name, metadata, spec, projectSelector);
  }

  @JsonIgnore
  public PipelineEntity updateEntity(Pipeline pipeline) {
    this.name = pipeline.getName();
    try {
      this.metadata = pipeline.getSerializedMetadata();
      this.spec = PipelineSpec.serializePipelineSpec(pipeline.getSpec().getSteps());
    } catch (JsonProcessingException e) {
      throw new JsonNotParsableException(e.getMessage());
    }
    return this;
  }

  public UUID getId() {
    return id;
  }

  public JsonNode getMetadata() {
    return metadata;
  }

  public JsonNode getSpec() {
    return spec;
  }
  public JsonNode getProjectSelector() {
    return projectSelector;
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

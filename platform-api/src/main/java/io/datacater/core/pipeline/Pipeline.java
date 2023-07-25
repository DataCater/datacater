package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.datacater.core.exceptions.JsonNotParsableException;
import io.datacater.core.utilities.JsonUtilities;

import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class Pipeline {
  @JsonProperty("name")
  private String name;

  @JsonProperty("metadata")
  @ElementCollection
  private Map<String, String> metadata;

  @JsonProperty("projectSelector")
  @ElementCollection
  private Map<String, String> projectSelector;

  @Embedded
  @JsonProperty("spec")
  private PipelineSpec spec;

  @ExcludeFromGeneratedCoverageReport
  protected Pipeline() {}

  private Pipeline(String name, Map<String, String> metadata, PipelineSpec spec, Map<String, String> projectSelector) {
    this.name = name;
    this.metadata = metadata;
    this.spec = spec;
    this.projectSelector = projectSelector;
  }

  @JsonCreator
  public static Pipeline from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "metadata", required = true) Map<String, String> metadata,
      @JsonProperty(value = "spec", required = true) PipelineSpec spec,
      @JsonProperty(value = "projectSelector", required = true) Map<String, String> projectSelector) {
    return new Pipeline(name, metadata, spec, projectSelector);
  }

  @JsonIgnore
  static Pipeline from(PipelineEntity pe) throws JsonProcessingException {
    Map<String, String> metadata = JsonUtilities.toStringMap(pe.getMetadata());
    PipelineSpec spec = PipelineSpec.from(pe.getSpec());
    Map<String, String> projectSelector = JsonUtilities.toStringMap(pe.getProjectSelector());
    return new Pipeline(pe.getName(), metadata, spec, projectSelector);
  }

  @JsonIgnore
  public Map<String, String> getMetadata() {
    return this.metadata;
  }

  @JsonIgnore
  public JsonNode getSerializedMetadata() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(objectMapper.writeValueAsString(this.metadata));
  }

  @ExcludeFromGeneratedCoverageReport
  @Override
  public String toString() {
    try {
      YAMLMapper yamlMapper = new YAMLMapper();
      yamlMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
      return yamlMapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonNotParsableException(e.getMessage());
    }
  }

  public String getName() {
    return name;
  }

  public PipelineSpec getSpec() {
    return spec;
  }
  public Map<String, String> getProjectSelector() {
    return projectSelector;
  }

  public void setSpec(PipelineSpec spec) {
    this.spec = spec;
  }
}

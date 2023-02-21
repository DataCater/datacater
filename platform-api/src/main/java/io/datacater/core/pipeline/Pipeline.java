package io.datacater.core.pipeline;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.datacater.core.exceptions.JsonNotParsableException;
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

  @Embedded
  @JsonProperty("spec")
  private PipelineSpec spec;

  @ElementCollection
  @JsonProperty("labels")
  Map<String, String> labels;

  @ExcludeFromGeneratedCoverageReport
  protected Pipeline() {}

  private Pipeline(String name, Map<String, String> metadata, PipelineSpec spec) {
    this.name = name;
    this.metadata = metadata;
    this.spec = spec;
  }

  private Pipeline(
      String name, Map<String, String> metadata, PipelineSpec spec, Map<String, String> labels) {
    this.name = name;
    this.metadata = metadata;
    this.spec = spec;
    this.labels = labels;
  }

  @JsonCreator
  public static Pipeline from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "metadata", required = true) Map<String, String> metadata,
      @JsonProperty(value = "spec", required = true) PipelineSpec spec) {
    return new Pipeline(name, metadata, spec);
  }

  @JsonIgnore
  static Pipeline from(PipelineEntity pe) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> metadata = mapper.readValue(pe.getSpec().asText(), Map.class);
    PipelineSpec spec = PipelineSpec.from(pe.getSpec());
    return new Pipeline(pe.getName(), metadata, spec);
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

  public void setSpec(PipelineSpec spec) {
    this.spec = spec;
  }
}

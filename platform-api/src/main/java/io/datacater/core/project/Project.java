package io.datacater.core.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.ExcludeFromGeneratedCoverageReport;
import io.datacater.core.exceptions.JsonNotParsableException;
import java.util.Map;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class Project {
  @JsonProperty("name")
  private String name;

  @Embedded
  @JsonProperty("spec")
  private Map<String, String> spec;

  @ExcludeFromGeneratedCoverageReport
  protected Project() {}

  private Project(String name, Map<String, String> spec) {
    this.name = name;
    this.spec = spec;
  }

  @JsonCreator
  public static Project from(
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "spec", required = true) Map<String, String> spec) {
    return new Project(name, spec);
  }

  @JsonIgnore
  static Project from(ProjectEntity pe) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> spec = mapper.readValue(pe.getSpec().asText(), Map.class);
    return new Project(pe.getName(), spec);
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

  public Map<String, String> getSpec() {
    return spec;
  }

  public void setSpec(Map<String, String> spec) {
    this.spec = spec;
  }
}

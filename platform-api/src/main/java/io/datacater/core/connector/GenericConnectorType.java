package io.datacater.core.connector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkiverse.hibernate.types.json.JsonTypes;
import javax.persistence.Column;
import org.hibernate.annotations.Type;

public class GenericConnectorType {

  @JsonProperty("image")
  String image;

  @JsonProperty("version")
  private String version;

  @JsonProperty("name")
  private String name;

  @Type(type = JsonTypes.JSON)
  @Column(name = "configuration", columnDefinition = JsonTypes.JSON_BIN)
  @JsonProperty("configuration")
  private JsonNode configuration;

  protected GenericConnectorType() {}

  private GenericConnectorType(String name, JsonNode configuration) {
    this.name = name;
    this.configuration = configuration;
  }

  @JsonCreator
  public static GenericConnectorType from(
      @JsonProperty("image") String image,
      @JsonProperty("version") String version,
      @JsonProperty("name") String name,
      @JsonProperty("configuration") JsonNode configuration) {
    return new GenericConnectorType(name, configuration);
  }

  public String getName() {
    return this.name;
  }

  public JsonNode getConfiguration() {
    return this.configuration;
  }
}

package io.datacater.core.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.logging.Logger;

public record ConnectorSpec(
    @JsonProperty(value = "name") @JsonSetter(nulls = Nulls.AS_EMPTY) String name,
    @Schema(
            example =
                """
            {
                "stream": "dc161a69-fa49-4b1a-b1b1-6d8246d50d72",
                "image": "datacater/concon-rss:latest"
              }
        """)
        @JsonProperty(value = "spec", required = true)
        Connector connector,
    @JsonProperty(value = "configSelector") @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, String> configSelector) {

  private static final Logger LOGGER = Logger.getLogger(ConnectorSpec.class);

  public ConnectorSpec(String name, Connector connector) {
    this(name, connector, new HashMap<>());
  }

  public ConnectorSpec(ConnectorSpec spec) {
    this(spec.name, spec.connector, spec.configSelector);
  }

  public Map<String, Object> getConnectorMap() {
    Map<String, Object> specMap =
        Map.of(
            "image", this.connector().image(),
            "stream", this.connector().stream(),
            "config", this.connector().config());
    return Map.of("name", this.name(), "spec", specMap, "configSelector", this.configSelector());
  }

  public static ConnectorSpec from(ConnectorSpec spec) {
    LOGGER.debug("ConnectorSpec created from spec with name: " + spec.name());
    return new ConnectorSpec(spec.name(), spec.connector(), spec.configSelector());
  }
}

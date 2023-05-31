package io.datacater.core.connector;

import io.quarkus.runtime.StartupEvent;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.enterprise.event.Observes;

public class ConnectorTypeInitializer {

  private static final Path PACKAGED_CONNECTOR_TYPES;

  static {
    try {
      PACKAGED_CONNECTOR_TYPES =
          Paths.get(ClassLoader.getSystemResource("connector_types").toURI());
    } catch (URISyntaxException e) {
      throw new ConnectorTypeInvalidException("Unable to load `connector_types` from within jar.");
    }
  }

  private static final List<Path> files = List.of(PACKAGED_CONNECTOR_TYPES);

  void onStart(@Observes StartupEvent startupEvent) {}
}

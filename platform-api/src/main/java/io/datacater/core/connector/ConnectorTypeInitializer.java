package io.datacater.core.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.datacater.core.exceptions.ConnectorTypeInvalidException;
import io.datacater.core.exceptions.JsonNotParsableException;
import io.quarkus.runtime.StartupEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import org.jboss.logging.Logger;

@Singleton
public class ConnectorTypeInitializer {
  private static final Logger LOGGER = Logger.getLogger(ConnectorTypeInitializer.class);
  private final List<ConnectorType> loadedConnectorTypes = new ArrayList<>();
  private final String JAR_CONNECTOR_TYPE_DIRECTORY = "connector_types";

  void onStart(@Observes StartupEvent startupEvent) {
    Path packagedConnectorTypes;
    var connectorTypes =
        Thread.currentThread().getContextClassLoader().getResource(JAR_CONNECTOR_TYPE_DIRECTORY);
    try {
      Objects.requireNonNull(connectorTypes);
      packagedConnectorTypes = Paths.get(connectorTypes.toURI());
    } catch (URISyntaxException use) {
      throw new ConnectorTypeInvalidException(
          "Unable to load `connector_types` from within jar.", use);
    } catch (NullPointerException npe) {
      throw new ConnectorTypeInvalidException(
          "System resource `connector_types` could not be found. This is indicates an invalid installation of DataCater",
          npe);
    }

    File jarConnectorTypes = packagedConnectorTypes.toFile();
    List<File> nonJarConnectorTypes =
        StaticConfig.EnvironmentVariables.CONNECTOR_TYPES_TO_LOAD.stream()
            .map(stringPath -> Paths.get(stringPath).toFile())
            .toList();
    loadedConnectorTypes.addAll(getConnectorTypesFromDirectory(jarConnectorTypes));
    for (File nonJarConnectorType : nonJarConnectorTypes) {
      loadedConnectorTypes.addAll(getConnectorTypesFromDirectory(nonJarConnectorType));
    }
    List<String> types = loadedConnectorTypes.stream().map(ConnectorType::name).toList();
    LOGGER.info("Loaded ConnectorTypes: [ " + String.join(",", types) + " ].");
  }

  List<ConnectorType> getLoadedConnectorTypes() {
    return loadedConnectorTypes;
  }

  private List<ConnectorType> getConnectorTypesFromDirectory(File directory) {
    List<Path> connectorTypeFiles = new ArrayList<>();
    String absolutePath = directory.getAbsolutePath();
    if (directory.isDirectory()) {
      for (String connectorTypeFile : Objects.requireNonNull(directory.list())) {
        Path fullyQualifiedPath =
            Paths.get(absolutePath + FileSystems.getDefault().getSeparator() + connectorTypeFile);
        connectorTypeFiles.add(fullyQualifiedPath);
      }
    } else {
      connectorTypeFiles.add(Paths.get(absolutePath));
    }

    return connectorTypeFiles.stream().map(this::mapYAMLToConnectorType).toList();
  }

  private ConnectorType mapYAMLToConnectorType(Path yaml) {
    try {
      YAMLMapper yamlMapper = new YAMLMapper();
      return yamlMapper.readValue(yaml.toFile(), ConnectorType.class);
    } catch (JsonProcessingException jpe) {
      throw new JsonNotParsableException(jpe.getMessage());
    } catch (IOException ioe) {
      throw new JsonNotParsableException(
          "Unable to parse file " + yaml.getFileName().toString() + ".", ioe);
    }
  }
}

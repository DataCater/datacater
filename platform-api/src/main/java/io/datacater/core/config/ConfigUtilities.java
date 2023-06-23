package io.datacater.core.config;

import io.datacater.core.config.enums.Kind;
import io.datacater.core.connector.ConnectorSpec;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.exceptions.IncorrectConfigException;
import io.datacater.core.stream.Stream;
import io.datacater.core.utilities.JsonUtilities;
import io.smallrye.mutiny.Uni;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class ConfigUtilities {

  private ConfigUtilities() {}

  public static Uni<List<ConfigEntity>> getMappedConfigs(
      Map<String, String> configs, Mutiny.Session session) {

    if (configs == null) {
      return Uni.createFrom().item(new ArrayList<>());
    }

    return session
        .createQuery("from ConfigEntity", ConfigEntity.class)
        .getResultList()
        .onItem()
        .transform(
            configEntityList ->
                configEntityList.stream()
                    .filter(
                        item ->
                            stringMapsContainsEqualKey(
                                JsonUtilities.toStringMap(
                                    item.getMetadata().get(StaticConfig.LABELS)),
                                configs))
                    .toList())
        .onItem()
        .ifNull()
        .continueWith(new ArrayList<>());
  }

  public static Stream applyConfigsToStream(Stream stream, List<ConfigEntity> configList) {
    depthSearchConfigsForDuplicateKeys(configList);
    for (ConfigEntity config : configList) {
      if (config.getId() != null) {
        verifyKindOfConfig(Kind.STREAM, config.getKind());
        stream
            .spec()
            .getKafka()
            .putAll(
                JsonUtilities.combineMaps(
                    JsonUtilities.toObjectMap(
                        config
                            .getSpec()
                            .get(stream.spec().getKind().name().toLowerCase(Locale.ROOT))),
                    stream.spec().getKafka()));
      }
    }
    return stream;
  }

  public static DeploymentSpec applyConfigsToDeployment(
      DeploymentSpec deploymentSpec, List<ConfigEntity> configList) {
    if (!configList.isEmpty()) {
      depthSearchConfigsForDuplicateKeys(configList);
      for (ConfigEntity config : configList) {
        if (config.getId() != null) {
          verifyKindOfConfig(Kind.DEPLOYMENT, config.getKind());
          deploymentSpec
              .deployment()
              .putAll(
                  JsonUtilities.combineMaps(
                      JsonUtilities.toObjectMap(config.getSpec()), deploymentSpec.deployment()));
        }
      }
    }
    return deploymentSpec;
  }

  public static ConnectorSpec applyConfigsToConnector(
      ConnectorSpec connectorSpec, List<ConfigEntity> configList) {
    if (!configList.isEmpty()) {
      depthSearchConfigsForDuplicateKeys(configList);
      for (ConfigEntity config : configList) {
        if (config.getId() != null) {
          verifyKindOfConfig(Kind.DEPLOYMENT, config.getKind());
          connectorSpec
              .getConnectorMap()
              .putAll(
                  JsonUtilities.combineMaps(
                      JsonUtilities.toObjectMap(config.getSpec()),
                      connectorSpec.getConnectorMap()));
        }
      }
    }
    return connectorSpec;
  }

  private static void verifyKindOfConfig(Kind expected, Kind actual) {
    if (expected != actual) {
      String exceptionMessage =
          String.format(
              StaticConfig.LoggerMessages.KIND_DOES_NOT_MATCH_EXCEPTION_MESSAGE_FORMATTED,
              actual,
              expected);
      throw new IncorrectConfigException(exceptionMessage);
    }
  }

  private static void depthSearchConfigsForDuplicateKeys(List<ConfigEntity> configList) {
    int skipFirstEntry = 1;
    int listFirstEntry = 0;
    if (configList.size() <= 1) {
      // nothing to compare
      return;
    }
    Map<String, Object> givenMap =
        JsonUtilities.toObjectMap(configList.get(listFirstEntry).getSpec());
    configList.stream()
        .skip(skipFirstEntry)
        .forEach(
            configEntity -> {
              Map<String, Object> currentMap = JsonUtilities.toObjectMap(configEntity.getSpec());
              String duplicateKey = mapsContainsEqualKey(givenMap, currentMap);
              if (duplicateKey != null) {
                String exceptionMessage =
                    String.format(
                        StaticConfig.LoggerMessages.KEY_EXISTS_TWICE_EXCEPTION_MESSAGE,
                        duplicateKey);
                throw new IncorrectConfigException(exceptionMessage);
              }
            });
  }

  private static String mapsContainsEqualKey(
      Map<String, Object> givenMap, Map<String, Object> currentMap) {
    for (Map.Entry<String, Object> currentEntry : currentMap.entrySet()) {
      if (givenMap.containsKey(currentEntry.getKey())) {
        if (currentEntry.getValue() instanceof HashMap<?, ?>) {
          return mapsContainsEqualKey(
              (Map<String, Object>) givenMap.get(currentEntry.getKey()),
              (Map<String, Object>) currentEntry.getValue());
        } else {
          return currentEntry.getKey();
        }
      }
    }
    return null;
  }

  private static boolean stringMapsContainsEqualKey(
      Map<String, String> givenMap, Map<String, String> currentMap) {
    if (givenMap == null || currentMap == null || givenMap.isEmpty() || currentMap.isEmpty()) {
      return false;
    }
    for (Map.Entry<String, String> givenEntry : givenMap.entrySet()) {
      if (currentMap.containsKey(givenEntry.getKey())
          && Objects.equals(givenEntry.getValue(), currentMap.get(givenEntry.getKey()))) {
        return true;
      }
    }
    return false;
  }
}

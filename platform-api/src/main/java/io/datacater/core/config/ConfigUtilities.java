package io.datacater.core.config;

import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.enums.Kind;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.exceptions.IncorrectConfigException;
import io.datacater.core.stream.Stream;
import io.datacater.core.utilities.JsonUtilities;
import io.smallrye.mutiny.Uni;
import java.util.*;
import org.jboss.logging.Logger;

public class ConfigUtilities {
  private static final Logger LOGGER = Logger.getLogger(ConfigUtilities.class);

  public static Uni<List<ConfigEntity>> getConfig(
      Map<String, String> configs, DataCaterSessionFactory dsf) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .createQuery("from ConfigEntity", ConfigEntity.class)
                .getResultList()
                .onItem()
                .transform(
                    x ->
                        x.stream()
                            .filter(
                                item ->
                                    stringMapsContainsEqualKey(
                                        JsonUtilities.toStringMap(item.getMetadata().get("labels")),
                                        configs))
                            .toList())
                .onItem()
                .ifNull()
                .continueWith(new ArrayList<>()));
  }

  public static Stream applyConfigsToStream(Stream stream, List<ConfigEntity> configList) {
    if (!configList.isEmpty()) {
      depthSearchConfigsForDuplicateKeys(configList);
      for (ConfigEntity config : configList) {
        if (config.getId() != null) {
          verifyKindOfConfig(Kind.STREAM, config.getKind());
          stream
              .spec()
              .getKafka()
              .putAll(
                  JsonUtilities.toObjectMap(
                      config
                          .getSpec()
                          .get(stream.spec().getKind().name().toLowerCase(Locale.ROOT))));
        }
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
          deploymentSpec.deployment().putAll(JsonUtilities.toObjectMap(config.getSpec()));
        }
      }
    }
    return deploymentSpec;
  }

  private static void verifyKindOfConfig(Kind expected, Kind actual) {
    if (expected != actual) {
      String ExceptionMessage =
          String.format(
              "The Config kind '%s' does not match that of the given resource '%s'",
              actual, expected);
      throw new IncorrectConfigException(ExceptionMessage);
    }
  }

  private static void depthSearchConfigsForDuplicateKeys(List<ConfigEntity> configList) {
    if (configList.size() <= 1) {
      // nothing to compare
      return;
    }
    Map<String, Object> givenMap = JsonUtilities.toObjectMap(configList.get(0).getSpec());
    configList.stream()
        .skip(1)
        .forEach(
            x -> {
              Map<String, Object> currentMap = JsonUtilities.toObjectMap(x.getSpec());
              String duplicateKey = mapsContainsEqualKey(givenMap, currentMap);
              if (duplicateKey != null) {
                String ExceptionMessage =
                    String.format(
                        "The key '%s' was found in at least two given Configs", duplicateKey);
                throw new IncorrectConfigException(ExceptionMessage);
              }
            });
  }

  private static String mapsContainsEqualKey(
      Map<String, Object> givenMap, Map<String, Object> currentMap) {
    for (String currentKey : currentMap.keySet()) {
      if (givenMap.containsKey(currentKey)) {
        Object value = currentMap.get(currentKey);
        if (value instanceof HashMap<?, ?>) {
          return mapsContainsEqualKey(
              (Map<String, Object>) givenMap.get(currentKey),
              (Map<String, Object>) currentMap.get(currentKey));
        } else {
          return currentKey;
        }
      }
    }
    return null;
  }

  private static boolean stringMapsContainsEqualKey(
      Map<String, String> givenMap, Map<String, String> currentMap) {
    LOGGER.info("logging maps: ");
    LOGGER.info(givenMap.entrySet());
    LOGGER.info(currentMap.entrySet());
    for (String currentKey : givenMap.keySet()) {
      LOGGER.info("checking key: " + currentKey + " with value: " + givenMap.get(currentKey));
      if (currentMap.containsKey(currentKey)) {
        if (Objects.equals(currentMap.get(currentKey), givenMap.get(currentKey))) {
          LOGGER.info("key: " + currentKey + ", has matched");
          return true;
        }
      }
    }
    return false;
  }
}

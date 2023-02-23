package io.datacater.core.config;

import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.enums.Kind;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.exceptions.IncorrectConfigException;
import io.datacater.core.stream.Stream;
import io.datacater.core.utilities.JsonUtilities;
import io.smallrye.mutiny.Uni;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigUtilities {

  public static Uni<List<ConfigEntity>> getConfig(
      List<String> configs, DataCaterSessionFactory dsf) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .createQuery("from ConfigEntity", ConfigEntity.class)
                .getResultList()
                .onItem()
                .transform(
                    x -> x.stream().filter(item -> configs.contains(item.getName())).toList())
                .onItem()
                .ifNull()
                .continueWith(new ArrayList<>()));
  }

  public static List<String> getConfigNames(Map<String, String> labels) {
    // TODO consider other label options
    return labels.entrySet().stream()
        .filter(x -> Objects.equals(x.getKey(), "app.datacater.io/name"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        .values()
        .stream()
        .toList();
  }

  public static Stream combineWithStream(Stream stream, List<ConfigEntity> configList) {
    if (!configList.isEmpty()) {
      validateConfigList(configList);
      for (ConfigEntity config : configList) {
        if (config.getId() != null) {
          checkValidKind(Kind.STREAM, config.getKind());
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

  public static DeploymentSpec combineWithDeployment(
      DeploymentSpec deploymentSpec, List<ConfigEntity> configList) {
    if (!configList.isEmpty()) {
      validateConfigList(configList);
      for (ConfigEntity config : configList) {
        if (config.getId() != null) {
          checkValidKind(Kind.DEPLOYMENT, config.getKind());
          deploymentSpec.deployment().putAll(JsonUtilities.toObjectMap(config.getSpec()));
        }
      }
    }
    return deploymentSpec;
  }

  private static void checkValidKind(Kind expected, Kind actual) {
    if (expected != actual) {
      String ExceptionMessage =
          String.format(
              "The Config kind '%s' does not match that of the given resource '%s'",
              actual, expected);
      throw new IncorrectConfigException(ExceptionMessage);
    }
  }

  private static void validateConfigList(List<ConfigEntity> configList) {
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
              String duplicateKey = mapsContainEqualKey(givenMap, currentMap);
              if (duplicateKey != null) {
                String ExceptionMessage =
                    String.format(
                        "The key '%s' was found in at least two given Configs", duplicateKey);
                throw new IncorrectConfigException(ExceptionMessage);
              }
            });
  }

  private static String mapsContainEqualKey(
      Map<String, Object> givenMap, Map<String, Object> currentMap) {
    for (String currentKey : currentMap.keySet()) {
      if (givenMap.containsKey(currentKey)) {
        Object value = currentMap.get(currentKey);
        if (value instanceof HashMap<?, ?>) {
          return mapsContainEqualKey(
              (Map<String, Object>) givenMap.get(currentKey),
              (Map<String, Object>) currentMap.get(currentKey));
        } else {
          return currentKey;
        }
      }
    }
    return null;
  }
}

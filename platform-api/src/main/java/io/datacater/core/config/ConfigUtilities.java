package io.datacater.core.config;

import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.config.enums.Kind;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.exceptions.IncorrectConfigKindException;
import io.datacater.core.pipeline.PipelineEntity;
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
                .ifNull()
                .continueWith(new ArrayList<ConfigEntity>()));
  }

  public static List<String> getConfigNames(Map<String, String> labels) {
    // TODO consider other label options
    return labels.entrySet().stream()
        .filter(x -> x.getKey() == "app.datacater.io/name")
        .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()))
        .values()
        .stream()
        .toList();
  }

  public static Stream combineWithStream(Stream stream, List<ConfigEntity> configList) {
    if (!configList.isEmpty()) {
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

  public static PipelineEntity combineWithPipeline(PipelineEntity pe, ConfigEntity config) {
    if (config.getId() != null) {
      checkValidKind(Kind.PIPELINE, config.getKind());
      JsonNode pipelineSpecNode = pe.getSpec();
      Map<String, Object> pipelineSpec = JsonUtilities.toObjectMap(pipelineSpecNode);
      Map<String, Object> configSpec = JsonUtilities.toObjectMap(config.getSpec());
      pipelineSpec.putAll(configSpec);

      // need to map steps from config
      // add the steps to pe
      // should the steps be added at the bottom of the steps or overwrite steps? has implications
      // and
      // the way records are transformed and filtered

      // TODO finish mapping
    }
    return pe;
  }

  public static DeploymentSpec combineWithDeployment(
      DeploymentSpec deploymentSpec, List<ConfigEntity> configList) {
    if (!configList.isEmpty()) {
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
      throw new IncorrectConfigKindException(ExceptionMessage);
    }
  }
}

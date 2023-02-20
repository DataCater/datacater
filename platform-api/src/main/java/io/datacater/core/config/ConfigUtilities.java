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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.jboss.logging.Logger;

public class ConfigUtilities {
  private static final Logger LOGGER = Logger.getLogger(ConfigUtilities.class);

  public static Uni<ConfigEntity> getConfig(UUID configUUID, DataCaterSessionFactory dsf) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(ConfigEntity.class, configUUID)
                .onItem()
                .ifNull()
                .continueWith(new ConfigEntity()));
  }

  public static UUID getConfigUUID(Map<String, String> labels) {
    // TODO consider other label options
    try {
      return UUID.fromString(labels.get("app.datacater.io/config"));
    } catch (Exception e) {
      return new UUID(0, 0);
    }
  }

  public static Stream combineWithStream(Stream stream, ConfigEntity config) {
    checkValidKind(Kind.STREAM, config.getKind());

    stream
        .spec()
        .getKafka()
        .putAll(
            JsonUtilities.toObjectMap(
                config.getSpec().get(stream.spec().getKind().name().toLowerCase(Locale.ROOT))));

    return stream;
  }

  public static PipelineEntity combineWithPipeline(PipelineEntity pe, ConfigEntity config) {
    checkValidKind(Kind.PIPELINE, config.getKind());
    JsonNode pipelineSpecNode = pe.getSpec();
    Map<String, Object> pipelineSpec = JsonUtilities.toObjectMap(pipelineSpecNode);
    Map<String, Object> configSpec = JsonUtilities.toObjectMap(config.getSpec());
    pipelineSpec.putAll(configSpec);

    // need to map steps from config
    // add the steps to pe
    // should the steps be added at the bottom of the steps or overwrite steps? has implications and
    // the way records are transformed and filtered

    // TODO finish mapping
    LOGGER.info(pe.asJsonString());
    return pe;
  }

  public static DeploymentSpec combineWithDeployment(
      DeploymentSpec deploymentSpec, ConfigEntity config) {
    checkValidKind(Kind.DEPLOYMENT, config.getKind());

    deploymentSpec.deployment().putAll(JsonUtilities.toObjectMap(config.getSpec()));

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

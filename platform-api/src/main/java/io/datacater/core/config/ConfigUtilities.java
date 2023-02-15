package io.datacater.core.config;

import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.deployment.DeploymentEntity;
import io.datacater.core.pipeline.Pipeline;
import io.datacater.core.stream.Stream;
import io.datacater.core.utilities.JsonUtilities;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import java.util.UUID;

public class ConfigUtilities {

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
    // TODO this needs to be done cleaner and consider not only top level spec, but also kafka spec
    stream.spec().getKafka().putAll(JsonUtilities.toMap(config.getSpec()));
    return stream;
  }

  public static void combineWithPipeline(Pipeline stream, ConfigEntity config) {}

  public static void combineWithDeployment(DeploymentEntity stream, ConfigEntity config) {}
}

package io.datacater.core.config;

import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.deployment.DeploymentEntity;
import io.datacater.core.pipeline.Pipeline;
import io.datacater.core.stream.Stream;
import io.datacater.core.utilities.JsonUtilities;
import io.smallrye.mutiny.Uni;
import java.util.Map;

public class ConfigUtilities {

  public static Uni<ConfigEntity> getConfig(String configUUID, DataCaterSessionFactory dsf) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(ConfigEntity.class, configUUID)
                .onItem()
                .ifNull()
                .continueWith(new ConfigEntity()));
  }

  public static String getConfigUUID(Map<String, String> labels) {
    return labels.get("app.datacater.io/config");
  }

  public static Stream combineWithStream(Stream stream, ConfigEntity config) {
    stream.spec().getConfig().putAll(JsonUtilities.toMap(config.getSpec()));
    return stream;
  }

  public static void combineWithPipeline(Pipeline stream, ConfigEntity config) {}

  public static void combineWithDeployment(DeploymentEntity stream, ConfigEntity config) {}
}

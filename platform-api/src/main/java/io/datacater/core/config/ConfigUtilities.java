package io.datacater.core.config;

import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.stream.StreamEntity;
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

  public static void combineWithStream(StreamEntity stream, ConfigEntity config) {}

  public static void combineWithPipeline(StreamEntity stream, ConfigEntity config) {}

  public static void combineWithDeployment(StreamEntity stream, ConfigEntity config) {}
}

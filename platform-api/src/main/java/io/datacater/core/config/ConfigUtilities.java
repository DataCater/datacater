package io.datacater.core.config;

import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.pipeline.PipelineEntity;
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
    // make a list of known config options for kafka topic (only things like bootstrap servers,
    // partitions and replications)
    // these should be manually mapped and the other options can be overwritten/added to the
    // underlying spec
    stream.spec().getKafka().putAll(JsonUtilities.toMap(config.getSpec()));
    return stream;
  }

  public static PipelineEntity combineWithPipeline(PipelineEntity pe, ConfigEntity config) {
    JsonNode pipelineSpecNode = pe.getSpec();
    Map<String, String> pipelineSpec = JsonUtilities.toMap(pipelineSpecNode);
    Map<String, String> configSPec = JsonUtilities.toMap(config.getSpec());
    pipelineSpec.putAll(configSPec);

    // need to map steps from config
    // add the steps to pe
    // should the steps be added at the bottom of the steps or overwrite steps? has implications and
    // the way records are transformed and filtered

    // TODO finish mapping
    return pe;
  }

  public static DeploymentSpec combineWithDeployment(
      DeploymentSpec deploymentSpec, ConfigEntity config) {
    // TODO
    // i think just overwriting/adding to deployment is fine, only one level and little config
    // options?
    deploymentSpec.deployment().putAll(JsonUtilities.toMap(config.getSpec()));
    return deploymentSpec;
  }
}

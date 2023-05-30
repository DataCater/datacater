package io.datacater.core.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import io.datacater.core.authentication.DataCaterSessionFactory;
import io.datacater.core.deployment.DeploymentSpec;
import io.datacater.core.exceptions.CreateDeploymentException;
import io.datacater.core.kubernetes.PythonRunnerPool;
import io.datacater.core.stream.StreamMessage;
import io.datacater.core.stream.StreamUtilities;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PipelineUtilities {
  @Inject PythonRunnerPool runnerPool;
  static final Logger LOGGER = Logger.getLogger(PipelineUtilities.class);
  @Inject DataCaterSessionFactory dsf;
  @Inject StreamUtilities streamUtil;

  public Uni<String> transformMessages(UUID uuid) {
    HttpClient httpClient = HttpClient.newHttpClient();

    Uni<PipelineEntity> pe = dsf.withSession(session -> session.find(PipelineEntity.class, uuid));
    Uni<List<StreamMessage>> messages =
        pe.flatMap(
            pipelineEntity -> {
              JsonNode streamIn = pipelineEntity.getMetadata().get(StaticConfig.STREAM_IN_TEXT);
              UUID streamUUID = UUID.fromString(streamIn.asText());
              return streamUtil.getStreamMessages(streamUUID);
            });

    Uni<PythonRunnerPool.NamedPod> namedPodAsync = runnerPool.getStaticPod();
    Uni<Tuple3<PipelineEntity, List<StreamMessage>, PythonRunnerPool.NamedPod>> combinedPeMsg =
        Uni.combine().all().unis(pe, messages, namedPodAsync).asTuple();

    return combinedPeMsg.flatMap(
        Unchecked.function(
            peMsg -> {
              PipelineEntity entity = peMsg.getItem1();
              List<StreamMessage> msgs = peMsg.getItem2();
              PythonRunnerPool.NamedPod namedPod = peMsg.getItem3();

              HttpRequest specPost =
                  namedPod.buildPost(entity.asJsonString(), StaticConfig.PIPELINE_PATH);
              CompletableFuture<HttpResponse<String>> specResponse =
                  httpClient.sendAsync(specPost, HttpResponse.BodyHandlers.ofString());

              LOGGER.info(specPost.uri().toString());
              StreamMessage recordMessagePayload = msgs.get(0);
              String messagesPayload = recordMessagePayload.toRecordJsonString();
              HttpRequest transformPost = namedPod.buildPost(messagesPayload);

              LOGGER.info(StaticConfig.PAYLOAD_SENT_UPDATE_MSG);
              LOGGER.info(messagesPayload);
              CompletableFuture<HttpResponse<String>> transformResponse =
                  httpClient.sendAsync(transformPost, HttpResponse.BodyHandlers.ofString());

              Uni<HttpResponse<String>> transform =
                  Uni.createFrom().completionStage(transformResponse);
              return Uni.createFrom()
                  .completionStage(specResponse)
                  .map(
                      response -> {
                        String message =
                            String.format(
                                StaticConfig.FormattedMessages.RECEIVED_RESPONSE_FORMATTED_MSG,
                                response.request().uri(),
                                response.statusCode());
                        LOGGER.info(message);
                        return response.body();
                      })
                  .flatMap(specPostResponse -> transform)
                  .map(HttpResponse::body);
            }));
  }

  public Uni<PipelineEntity> getPipelineFromDeployment(DeploymentSpec deploymentSpec) {
    return dsf.withTransaction(
        (session, transaction) ->
            session
                .find(PipelineEntity.class, getPipelineUUIDFromMap(deploymentSpec.deployment()))
                .onItem()
                .ifNull()
                .failWith(new CreateDeploymentException(StaticConfig.PIPELINE_NOT_FOUND)));
  }

  public UUID getPipelineUUIDFromMap(Map<String, Object> map) {
    try {
      return UUID.fromString(map.get(StaticConfig.PIPELINE_NODE_TEXT).toString());
    } catch (Exception e) {
      throw new CreateDeploymentException(StaticConfig.PIPELINE_NOT_FOUND);
    }
  }
}

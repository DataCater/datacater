package io.datacater;

import io.datacater.exceptions.TransformationException;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.json.JsonObject;
import io.smallrye.reactive.messaging.kafka.Record;

import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class Pipeline {
  private static final Logger LOGGER = Logger.getLogger(Pipeline.class);

  private final Integer DATACATER_PYTHONRUNNER_PORT =
          ConfigProvider.getConfig()
                  .getOptionalValue("datacater.python-runner.port", Integer.class)
                  .orElse(50000);

  private final String DATACATER_PYTHONRUNNER_HOST =
          ConfigProvider.getConfig()
                  .getOptionalValue("datacater.python-runner.host", String.class)
                  .orElse("localhost");

  private String host;
  private Integer port;

  private static final String PIPELINE_IN = "pipeline-in";
  private static final String PIPELINE_OUT = "pipeline-out";
  private static final String PIPELINE_ERROR_MSG = "Pipeline could not process message.\n Key: %s\n Value: %s";

  WebClient client;

  @Inject
  void VertxResource(Vertx vertx) {
    this.client = WebClient.create(vertx);
  }

  @Incoming(PIPELINE_IN)
  @Outgoing(PIPELINE_OUT)
  @Blocking
  @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
  public Record<UUID, JsonObject> processUUID(Record<UUID, JsonObject> message) {
    // This is deliberately blocking
    JsonObject transformedMessage = handleMessage(message);
    return Record.of(message.key(), transformedMessage);
  }

  private JsonObject handleMessage(Record<UUID, JsonObject> message) {
    HttpRequest<Buffer> request = client.post(getPort(), getHost(), "");
    HttpResponse<Buffer> response = request.sendJson(message.value().encode()).await().indefinitely();

    if(response.statusCode() != RestResponse.StatusCode.OK){
      String errorMsg = String.format(PIPELINE_ERROR_MSG, message.key(), response.bodyAsJsonObject().encodePrettily());
      LOGGER.error(errorMsg);
      throw new TransformationException(errorMsg);
    }

    return response.bodyAsJsonObject().getJsonObject("value");
  }

  protected void setNetwork(int port, String host){
    this.host = host;
    this.port = port;
  }

  private String getHost(){
    if(host != null){
      return host;
    }
    return DATACATER_PYTHONRUNNER_HOST;
  }

  private int getPort(){
    if (port != null){
      return port;
    }
    return DATACATER_PYTHONRUNNER_PORT;
  }
}

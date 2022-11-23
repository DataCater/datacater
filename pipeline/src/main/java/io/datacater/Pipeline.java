package io.datacater;

import io.datacater.exceptions.TransformationException;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.smallrye.reactive.messaging.kafka.Record;

import java.time.Duration;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.ConnectException;
import java.util.concurrent.CompletionException;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.eclipse.microprofile.reactive.messaging.*;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class Pipeline {
  private static final Logger LOGGER = Logger.getLogger(Pipeline.class);
  private String host;
  private Integer port;

  WebClient client;

  @Inject
  void VertxResource(Vertx vertx) {
    this.client = WebClient.create(vertx);
  }

  @Inject
  @Channel(PipelineConfig.STREAM_OUT)
  Emitter<Record<JsonObject, JsonObject>> producer;

  @Incoming(PipelineConfig.STREAM_IN)
  @Blocking
  @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
  public void processUUID(ConsumerRecords<JsonObject, JsonObject> messages) {
    try {
      // This is deliberately blocking
      handleMessages(messages);
    } catch (CompletionException | ConnectException e) {
      LOGGER.warn("Connection to Python-Runner sidecar failed: %s. Attempt: 0".format(e.getMessage()), e);

      int retries = 0;

      // Perform `PipelineConfig.CONNECTION_RETRIES` connection attempts
      while (retries < PipelineConfig.CONNECTION_RETRIES) {
        try {
          // Wait `PipelineConfig.CONNECTION_RETRY_WAIT` milliseconds before performing the next connection attempt
          Thread.sleep(PipelineConfig.CONNECTION_RETRY_WAIT);
          handleMessages(messages);
          break;
        } catch (CompletionException | ConnectException ce) {
          retries = retries + 1;
          LOGGER.warn(
                  "Connection to Python-Runner sidecar failed: %s. Attempt: %d".format(ce.getMessage(), retries),
                  ce);
        } catch (InterruptedException ie) {
          LOGGER.error("Thread got interrupted", ie);
          throw new RuntimeException(ie);
        }
      }

      // Fail if we saturated all connection attempts
      if (retries >= PipelineConfig.CONNECTION_RETRIES) {
        throw new TransformationException("All connection attempts to Python-Runner sidecar failed.");
      }
    }
  }

  private void handleMessages(ConsumerRecords<JsonObject, JsonObject> messages) throws ConnectException {
    HttpRequest<Buffer> request = client.post(getPort(), getHost(), PipelineConfig.ENDPOINT)
            .putHeader(PipelineConfig.HEADER, PipelineConfig.HEADER_TYPE);
    HttpResponse<Buffer> response = request.sendJson(getMessages(messages)).await().atMost(Duration.ofSeconds(PipelineConfig.DATACATER_PYTHONRUNNER_TIMEOUT));

    if(response.statusCode() != RestResponse.StatusCode.OK){
      LOGGER.error(response.bodyAsJsonObject().encodePrettily());
    } else {
      sendMessages(response.bodyAsJsonArray());
    }
  }

  private void sendMessages(JsonArray messages){
    messages.stream().forEach(x -> {
      if(x instanceof JsonObject json){
        if(json.getJsonObject(PipelineConfig.METADATA).containsKey(PipelineConfig.ERROR)){
          logMessage(json.encodePrettily());
        }
        sendRecord(Record.of(getKey(json), json.getJsonObject(PipelineConfig.VALUE)));
      }
    });
  }

  private void sendRecord(Record<JsonObject, JsonObject> record){
    producer.send(record);
  }

  private void logMessage(String message){
    String errorMsg = String.format(PipelineConfig.PIPELINE_ERROR_MSG, message);
    LOGGER.error(errorMsg);
  }

  private JsonObject getKey(JsonObject message){
    if(message.getJsonObject(PipelineConfig.KEY) == null){
      return null;
    }
    return message.getJsonObject(PipelineConfig.KEY);
  }

  private JsonArray getMessages(ConsumerRecords<JsonObject, JsonObject> messages){
    JsonArray jsonMessages = new JsonArray();
    for (ConsumerRecord<JsonObject, JsonObject> message:messages) {
      jsonMessages.add(new JsonObject().put(PipelineConfig.KEY, message.key()).put(PipelineConfig.VALUE, message.value()).put(PipelineConfig.METADATA, new JsonObject().put(PipelineConfig.OFFSET, message.offset()).put(PipelineConfig.PARTITION, message.partition())));
    }
    return jsonMessages;
  }

  protected void setNetwork(int port, String host){
    this.host = host;
    this.port = port;
  }

  private String getHost(){
    if(host != null){
      return host;
    }
    return PipelineConfig.DATACATER_PYTHONRUNNER_HOST;
  }

  private int getPort(){
    if (port != null){
      return port;
    }
    return PipelineConfig.DATACATER_PYTHONRUNNER_PORT;
  }
}

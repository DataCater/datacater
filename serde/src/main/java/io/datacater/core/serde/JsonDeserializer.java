package io.datacater.core.serde;

import io.datacater.core.exceptions.JsonDeserializationException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.common.serialization.Deserializer;

public class JsonDeserializer implements Deserializer<JsonObject> {

  @Override
  public JsonObject deserialize(String topic, byte[] data) {
    try {
      Buffer buffer = Buffer.buffer(data);
      return new JsonObject(buffer);
    } catch (Exception e) {
      throw new JsonDeserializationException(e.getMessage());
    }
  }
}

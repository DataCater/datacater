package io.datacater.core.serde;

import io.datacater.core.exceptions.JsonDeserializationException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;

public class JsonDeserializer implements Deserializer<Map<String, Object>> {

  @Override
  public Map<String, Object> deserialize(String topic, byte[] data) {
    if (data == null){
      return null;
    }
    try {
      Buffer buffer = Buffer.buffer(data);
      JsonObject obj = new JsonObject(buffer);
      return obj.getMap();
    } catch (Exception e) {
      throw new JsonDeserializationException(e.getMessage());
    }
  }
}

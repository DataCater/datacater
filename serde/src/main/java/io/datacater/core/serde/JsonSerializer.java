package io.datacater.core.serde;

import io.vertx.core.json.JsonObject;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerializer implements Serializer<JsonObject> {

    @Override
    public byte[] serialize(String topic, JsonObject data) {
        return data.encode().getBytes();
    }
}

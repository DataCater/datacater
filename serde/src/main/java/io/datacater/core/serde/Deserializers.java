package io.datacater.core.serde;

import org.apache.kafka.common.serialization.*;

import java.util.HashMap;
import java.util.Map;

public class Deserializers {
    public static final Map<String, Deserializer> deserializers;

    static {
        deserializers = new HashMap<>();
        deserializers.put("io.datacater.core.serde.JsonDeserializer", new JsonDeserializer());
        deserializers.put("io.datacater.core.serde.AvroDeserializer", new AvroDeserializer());
        deserializers.put("org.apache.kafka.common.serialization.StringDeserializer", new StringDeserializer());
        deserializers.put("org.apache.kafka.common.serialization.IntegerDeserializer", new IntegerDeserializer());
        deserializers.put("org.apache.kafka.common.serialization.LongDeserializer", new LongDeserializer());
        deserializers.put("org.apache.kafka.common.serialization.FloatDeserializer", new FloatDeserializer());
        deserializers.put("org.apache.kafka.common.serialization.DoubleDeserializer", new DoubleDeserializer());
        deserializers.put("org.apache.kafka.common.serialization.ShortDeserializer", new ShortDeserializer());
    }
}

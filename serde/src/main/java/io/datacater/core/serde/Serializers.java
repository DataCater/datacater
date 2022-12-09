package io.datacater.core.serde;

import org.apache.kafka.common.serialization.*;

import java.util.HashMap;
import java.util.Map;

public class Serializers {
    public static final Map<String, Serializer> serializers;

    static {
        serializers = new HashMap<>();
        serializers.put("io.datacater.core.serde.JsonSerializer", new JsonSerializer());
        serializers.put("io.datacater.core.serde.AvroSerializer", new AvroSerializer());
        serializers.put("org.apache.kafka.common.serialization.StringSerializer", new StringSerializer());
        serializers.put("org.apache.kafka.common.serialization.IntegerSerializer", new IntegerSerializer());
        serializers.put("org.apache.kafka.common.serialization.LongSerializer", new LongSerializer());
        serializers.put("org.apache.kafka.common.serialization.FloatSerializer", new FloatSerializer());
        serializers.put("org.apache.kafka.common.serialization.DoubleSerializer", new DoubleSerializer());
        serializers.put("org.apache.kafka.common.serialization.ShortSerializer", new ShortSerializer());
    }
}

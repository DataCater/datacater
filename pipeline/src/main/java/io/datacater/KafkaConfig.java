package io.datacater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacater.exceptions.KafkaConfigurationException;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.smallrye.common.annotation.Identifier;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;
import java.util.Map;

public class KafkaConfig {
    static private final String errorMsg= "The given Kafka Configuration could not be mapped: %s";
    static final String KEY_DESERIALIZER =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.serde.key.deserializer", String.class)
                    .orElse("io.datacater.core.serde.JsonDeserializer");
    static final String VALUE_DESERIALIZER =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.serde.value.deserializer", String.class)
                    .orElse("io.datacater.core.serde.JsonDeserializer");
    static final String KEY_SERIALIZER =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.serde.key.serializer", String.class)
                    .orElse("io.datacater.core.serde.JsonSerializer");
    static final String VALUE_SERIALIZER =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.serde.value.serializer", String.class)
                    .orElse("io.datacater.core.serde.JsonSerializer");
    static final String DATACATER_STREAMIN_TOPIC =
            ConfigProvider.getConfig()
                    .getOptionalValue("mp.messaging.incoming.streamin.topic", String.class)
                    .orElse("streamin");
    static final String DATACATER_STREAMOUT_TOPIC =
            ConfigProvider.getConfig()
                    .getOptionalValue("mp.messaging.incoming.streamout.topic", String.class)
                    .orElse("streamout");
}

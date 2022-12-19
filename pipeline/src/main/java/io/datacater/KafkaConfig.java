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
    static final String DATACATER_STREAMIN_CONFIG =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.streamin.config", String.class)
                    .orElse( "{\"key.deserializer\": \"io.datacater.core.serde.JsonDeserializer\", \"value.deserializer\": \"io.datacater.core.serde.JsonDeserializer\"}");

    static final String DATACATER_STREAMIN_TOPIC =
            ConfigProvider.getConfig()
                    .getOptionalValue("mp.messaging.incoming.streamin.topic", String.class)
                    .orElse("streamin");

    static final String DATACATER_STREAMOUT_TOPIC =
            ConfigProvider.getConfig()
                    .getOptionalValue("mp.messaging.incoming.streamout.topic", String.class)
                    .orElse("streamout");

    static final String DATACATER_STREAMOUT_CONFIG =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.streamout.config", String.class)
                    .orElse("{\"key.serializer\": \"io.datacater.core.serde.JsonSerializer\", \"value.serializer\": \"io.datacater.core.serde.JsonSerializer\"}");

    @Produces
    @ApplicationScoped
    @Identifier("streamin-configuration")
    public static Map<String, Object> streamInConfig() {
        return mapConfig(DATACATER_STREAMIN_CONFIG);
    }

    @Produces
    @ApplicationScoped
    @Identifier("streamout-configuration")
    public static Map<String, Object> streamOutConfig() {
        return mapConfig(DATACATER_STREAMOUT_CONFIG);
    }

    private static Map<String, Object> mapConfig(String json){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map;
        try{
            map = mapper.readValue(json, new TypeReference<>(){});
        } catch (JsonProcessingException e){
            throw new KafkaConfigurationException(String.format(errorMsg, e.getMessage()));
        }
        return map;
    }
}

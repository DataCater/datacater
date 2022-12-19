package io.datacater;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.*;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class PipelineTest {
    private static final Logger LOGGER = Logger.getLogger(PipelineTest.class);
    private static final String STREAMIN = "streamin-test";
    private static final String STREAMOUT = "streamout";
    @Inject
    Pipeline application;

    @Inject
    @Channel(STREAMIN)
    Emitter<ProducerRecord<JsonObject, JsonObject>> producer;

    @InjectKafkaCompanion
    KafkaCompanion companion;


    private static final Integer DATACATER_PYTHONRUNNER_PORT =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.python-runner.port", Integer.class)
                    .orElse(50000);

    private static final String DATACATER_PYTHONRUNNER_IMAGE =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.python-runner.image", String.class)
                    .orElse("datacater/python-runner");

    private static final String DATACATER_PYTHONRUNNER_VERSION =
            ConfigProvider.getConfig()
                    .getOptionalValue("datacater.python-runner.version", String.class)
                    .orElse("alpha");


    private static final GenericContainer<?> pythonRunner = new GenericContainer<>(DockerImageName.parse(DATACATER_PYTHONRUNNER_IMAGE).withTag(DATACATER_PYTHONRUNNER_VERSION))
            .withExposedPorts(DATACATER_PYTHONRUNNER_PORT).waitingFor(Wait.forHttp("/health"));

    @BeforeEach
    void setUp() {
        pythonRunner.start();
    }

    @Test
    void testStandardPipeline() throws ExecutionException, InterruptedException, TimeoutException {
        application.setNetwork(pythonRunner.getMappedPort(DATACATER_PYTHONRUNNER_PORT), pythonRunner.getHost());



        for(int i = 1; i < 300; i++){
            JsonObject message = new JsonObject();
            message.put("name", "Max Mustermann");
            message.put("email", "max-mustermann@datacater.io");
            message.put("is_admin", "true");
            producer.send(new ProducerRecord<>(
                    "stream-in",
                    new JsonObject().put("value", UUID.randomUUID()),
                    message.put("number", i)));
        }
        JsonObject message = new JsonObject();
        message.put("name", "Max Mustermann");
        message.put("email", "max-mustermann@datacater.io");
        message.put("is_admin", "true");
        CompletionStage<Void> messageToWaitOn = producer.send(new ProducerRecord<>(
                "stream-in",
                new JsonObject().put("value", UUID.randomUUID()),
                message));

        messageToWaitOn.toCompletableFuture().get(1000, TimeUnit.MILLISECONDS);

        ConsumerTask<Object, Object> messages = companion
                .consumeWithDeserializers(
                        io.datacater.core.serde.JsonDeserializer.class,
                        io.datacater.core.serde.JsonDeserializer.class)
                .fromTopics(STREAMOUT,1)
                .awaitCompletion();
        assertTrue(messages.getFirstRecord().value().toString().contains("max-mustermann@datacater.io"));
        assertEquals(1, messages.count());
    }
}

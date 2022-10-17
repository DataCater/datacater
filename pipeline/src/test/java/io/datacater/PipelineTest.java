package io.datacater;

import io.datacater.exceptions.TransformationException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.Record;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.*;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
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
    private static final String PIPELINE_IN = "pipeline-in-test";
    private static final String PIPELINE_OUT = "pipeline-out";
    @Inject
    Pipeline application;

    @Inject
    @Channel(PIPELINE_IN)
    Emitter<ProducerRecord<UUID, JsonObject>> producer;

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

         JsonObject message = new JsonObject();
         message.put("name", "Max Mustermann");
         message.put("email", "max-mustermann@datacater.io");
         message.put("is_admin", "true");

        CompletionStage<Void> messageToWaitOn = producer.send( new ProducerRecord<>(
                "pipeline-in",
                UUID.randomUUID(),
                message));

        messageToWaitOn.toCompletableFuture().get(1000, TimeUnit.MILLISECONDS);

        ConsumerTask<Object, Object> messages = companion.consumeWithDeserializers(org.apache.kafka.common.serialization.UUIDDeserializer.class, io.datacater.core.serde.JsonDeserializer.class).fromTopics(PIPELINE_OUT,1).awaitCompletion();
        assertTrue(messages.getFirstRecord().value().toString().contains("max-mustermann@datacater.io"));
        assertEquals(1, messages.count());
    }
}

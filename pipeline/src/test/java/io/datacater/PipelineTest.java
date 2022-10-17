package io.datacater;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
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

@QuarkusTest
class PipelineTest {
    private static final Logger LOGGER = Logger.getLogger(PipelineTest.class);

    private static final String PIPELINE_IN = "pipeline-in-test";
    @Inject
    Pipeline application;

    @Inject
    @Channel(PIPELINE_IN)
    Emitter<ProducerRecord<UUID, JsonObject>> producer;


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
    void vertxResource() throws ExecutionException, InterruptedException, TimeoutException {
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




        //TODO (ChrisRousey): consume message from "pipeline-out" and check if a standard transform worked
        //TODO (ChrisRousey): need a working example of python runner message, right now python runner always has error
    }

}

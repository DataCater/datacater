package io.datacater.core.kubernetes;

import io.datacater.core.kubernetes.KubernetesTestProfiles.PythonRunnerPoolTestProfile;
import io.datacater.core.kubernetes.PythonRunnerPool.NamedPod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.Deque;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTest
@WithKubernetesTestServer
@TestProfile(PythonRunnerPoolTestProfile.class)
@TestInstance(Lifecycle.PER_CLASS)
class PythonRunnerPoolTest {

  @KubernetesTestServer KubernetesServer testServer;

  @Inject PythonRunnerPool runnerPool;

  final Pod genericPod = new PodBuilder().build();

  @BeforeAll
  void init() {
    NamedPod pod1 = new NamedPod("Hakan", genericPod);
    var initialise = runnerPool.initialiseEmptyQueue();
    var putPod = runnerPool.putPod(pod1);
    var result = runnerPool.executeOnQueue(Deque::size);
    var uniChain = initialise.chain(() -> putPod).chain(() -> result);
    var assertSubscriber = uniChain.subscribe().withSubscriber(UniAssertSubscriber.create());

    assertSubscriber.awaitItem().assertCompleted();
  }

  @Test
  void testPutPod() {
    var result = runnerPool.executeOnQueue(Deque::size);
    var resultSubscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

    resultSubscriber.awaitItem().assertCompleted().assertItem(1);
  }

  @Test
  void testGetPod() {
    var getPod = runnerPool.getPod();
    var result = getPod.chain(namedPod -> runnerPool.executeOnQueue(Deque::size));
    var resultSubscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

    resultSubscriber.awaitItem().assertCompleted().assertItem(0);
  }
}

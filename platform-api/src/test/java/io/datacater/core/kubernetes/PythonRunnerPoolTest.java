package io.datacater.core.kubernetes;

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;

@QuarkusTest
@WithKubernetesTestServer
@TestProfile(KubernetesTestProfiles.PythonRunnerPoolTest.class)
class PythonRunnerPoolTest {

  @KubernetesTestServer KubernetesServer testServer;

  @Ignore
  void testInitialize() {
    var statefulSets =
        testServer
            .getClient()
            .apps()
            .statefulSets()
            .inNamespace("datacater")
            .withLabels(DataCaterK8sConfig.LABELS);

    Assertions.assertEquals(1, statefulSets.list().getItems().size());
  }
}

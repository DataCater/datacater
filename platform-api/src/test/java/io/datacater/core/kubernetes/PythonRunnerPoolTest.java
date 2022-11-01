package io.datacater.core.kubernetes;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
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
    Namespace ns =
        new NamespaceBuilder().withNewMetadata().withName("datacater").endMetadata().build();
    testServer.getClient().namespaces().createOrReplace(ns);

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

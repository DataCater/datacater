package io.datacater.core.kubernetes;

import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import java.util.Map;
import javax.inject.Inject;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;

@WithKubernetesTestServer
@QuarkusTest
@TestProfile(KubernetesTestProfiles.LabeledStatefulSetTest.class)
class LabeledStatefulSetTest {

  @Inject LabeledStatefulSet labeledSet;

  @KubernetesTestServer KubernetesServer testServer;

  static final Map<String, String> SELECTORS = Map.of("app.kubernetes.io/name", "python-runner");
  static final String SERVICE_NAME = "pythonrunner";

  @Ignore
  void createStatefulSetTest() {
    labeledSet.setClient(testServer.getClient());
    StatefulSet sts = labeledSet.blueprint();
    labeledSet.createStatefulSet(sts);
    var statefulSets =
        testServer.getClient().apps().statefulSets().inNamespace("datacater").withLabels(SELECTORS);

    Assertions.assertEquals(1, statefulSets.list().getItems().size());
    Assertions.assertEquals(1, statefulSets.list().getItems().get(0).getSpec().getReplicas());
    Assertions.assertEquals(
        SERVICE_NAME, statefulSets.list().getItems().get(0).getSpec().getServiceName());
  }
}

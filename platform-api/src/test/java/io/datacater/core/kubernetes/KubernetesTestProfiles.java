package io.datacater.core.kubernetes;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class KubernetesTestProfiles {

  public static class LabeledStatefulSetTest implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("datacater.pythonrunner.image.name", "ghcr.io/datacater/python-runner");
    }

    @Override
    public boolean disableApplicationLifecycleObservers() {
      return true;
    }
  }

  public static class PythonRunnerPoolTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("datacater.pythonrunner.image.name", "ghcr.io/datacater/python-runner");
    }
  }
}

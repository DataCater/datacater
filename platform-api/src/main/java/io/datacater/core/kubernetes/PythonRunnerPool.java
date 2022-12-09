package io.datacater.core.kubernetes;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.shareddata.AsyncMap;
import io.vertx.mutiny.core.shareddata.SharedData;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PythonRunnerPool {

  SharedData sharedData;

  @Inject
  public PythonRunnerPool(io.vertx.mutiny.core.Vertx vertx) {
    this.sharedData = vertx.sharedData();
  }

  @Inject KubernetesClient kubernetesClient;

  @Inject LabeledStatefulSet labeledStatefulSet;

  private static final Logger LOGGER = Logger.getLogger(PythonRunnerPool.class);
  private static final String POOL_NAME = "python-runner";

  private static final String RUNNER_EVAL_PATH = "/";

  static class RunnerPool {

    Deque<NamedPod> pool;

    RunnerPool(Deque<NamedPod> pool) {
      this.pool = pool;
    }

    Deque<NamedPod> getPool() {
      return this.pool;
    }
  }

  public record NamedPod(String name, Pod pod) {

    public HttpRequest buildPost(String payload) {
      return buildPost(payload, RUNNER_EVAL_PATH);
    }

    public HttpRequest buildPost(String payload, String path) {
      String namespace = this.pod.getMetadata().getNamespace();
      List<ContainerPort> first = this.pod.getSpec().getContainers().get(0).getPorts();
      Integer port = first.get(0).getContainerPort();
      String service = DataCaterK8sConfig.PYTHON_RUNNER_SERVICE_NAME;
      String uriReady =
          String.format(
              "http://%s.%s.%s.svc.cluster.local:%d%s",
              this.name, service, namespace, port, prependSlash(path));

      return HttpRequest.newBuilder()
          .POST(BodyPublishers.ofString(payload))
          .version(Version.HTTP_1_1)
          .uri(URI.create(uriReady))
          .build();
    }

    private String prependSlash(String path) {
      if (Boolean.FALSE.equals(path.startsWith("/"))) {
        return String.format("/%s", path);
      }
      return path;
    }
  }

  @PostConstruct
  void initialize(@Observes StartupEvent event) {
    LOGGER.info(
        String.format(
            "Initialising StatefulSet with replicas := %d",
            DataCaterK8sConfig.PYTHON_RUNNER_REPLICAS));
    labeledStatefulSet.setClient(kubernetesClient);
    StatefulSet statefulSet = labeledStatefulSet.blueprint();
    labeledStatefulSet.createStatefulSet(statefulSet);
    labeledStatefulSet.createService();
  }

  public Uni<NamedPod> getPod() {
    return getQueue()
        .onItem()
        .transform(
            queue -> {
              var pod = queue.pop();
              LOGGER.info(
                  String.format("Returning pod with name %s for interactive usage.", pod.name));
              return pod;
            });
  }

  public Uni<Deque<NamedPod>> getQueue() {
    Uni<AsyncMap<String, Deque<NamedPod>>> defaultMap = sharedData.getAsyncMap(POOL_NAME);

    return defaultMap
        .call(map -> map.putIfAbsent(POOL_NAME, podsFromKubernetes()))
        .chain(map -> map.get(POOL_NAME))
        .chain(
            queue -> {
              if (queue.isEmpty()) {
                LOGGER.info("Pool of Python Runners is empty. Re-filling with pods in cluster.");
                return refillQueue(); // reset queue if all pods were used
              } else {
                return Uni.createFrom().item(queue);
              }
            });
  }

  public Uni<Deque<NamedPod>> refillQueue() {
    Uni<AsyncMap<String, Deque<NamedPod>>> asyncMap = sharedData.getAsyncMap(POOL_NAME);
    Deque<NamedPod> refill = podsFromKubernetes();

    return asyncMap.onItem().call(map -> map.put(POOL_NAME, refill)).replaceWith(() -> refill);
  }

  public Uni<Void> initialiseEmptyQueue() {
    Uni<AsyncMap<String, Deque<NamedPod>>> asyncMap = sharedData.getAsyncMap(POOL_NAME);

    return asyncMap.onItem().transformToUni(map -> map.put(POOL_NAME, new ArrayDeque<>()));
  }

  public Uni<Deque<NamedPod>> putPod(NamedPod pod) {
    // Fetch AsyncMap called POOL_NAME
    // Fetch Deque called POOL_NAME
    // Put pod in Deque POOL_NAME
    // Put update Deque in AsyncMap
    // Return AsyncMap
    return getQueue()
        .onItem()
        .transform(
            queue -> {
              queue.push(pod);
              return queue;
            });
  }

  public Uni<Integer> executeOnQueue(Function<Deque<NamedPod>, Integer> applicable) {
    Uni<AsyncMap<String, Deque<NamedPod>>> uniMap = sharedData.getAsyncMap(POOL_NAME);

    // flatMap is equivalent to onItem().transformToUni()
    return uniMap.flatMap(asyncMap -> asyncMap.get(POOL_NAME)).map(applicable);
  }

  public Uni<NamedPod> getStaticPod() {
    RunnerPool pool = newPool().get();
    NamedPod np = pool.getPool().getFirst();
    return Uni.createFrom().item(np);
  }

  Supplier<RunnerPool> newPool() {
    return () -> {
      Deque<NamedPod> namedPods = podsFromKubernetes();

      return new RunnerPool(namedPods);
    };
  }

  private Deque<NamedPod> podsFromKubernetes() {
    List<Pod> pods =
        kubernetesClient
            .pods()
            .inNamespace(DataCaterK8sConfig.NAMESPACE)
            .withLabels(DataCaterK8sConfig.LABELS)
            .withField("status.phase", "Running")
            .list()
            .getItems();
    Deque<NamedPod> namedPods = new ArrayDeque<>(DataCaterK8sConfig.PYTHON_RUNNER_REPLICAS);
    for (Pod pod : pods) {
      String podName = pod.getMetadata().getName();
      namedPods.add(new NamedPod(podName, pod));
    }
    return namedPods;
  }
}

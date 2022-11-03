package io.datacater.core.kubernetes;

import io.datacater.core.exceptions.DatacaterException;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.shareddata.AsyncMap;
import io.vertx.mutiny.core.shareddata.Lock;
import io.vertx.mutiny.core.shareddata.SharedData;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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

  private static final String IN_USE_LABEL_KEY = "datacater.io/in-use";
  private static final String IN_USE_TRUE_LABEL_VALUE = "true";
  private static final String IN_USE_FALSE_LABEL_VALUE = "false";
  private static final Map<String, String> IN_USE_TRUE_LABEL =
      Map.of(IN_USE_LABEL_KEY, IN_USE_TRUE_LABEL_VALUE);
  private static final Map<String, String> IN_USE_FALSE_LABEL =
      Map.of(IN_USE_LABEL_KEY, IN_USE_FALSE_LABEL_VALUE);
  private static final Long LOCK_TIMEOUT_MS = 10000L;

  private static final int EXPECTED_STATEFUL_SETS = 1;

  private boolean IS_POOL_INITIALISED = false;

  private static final String RUNNER_EVAL_PATH = "/";

  static class RunnerPool {

    Deque<NamedPod> pool;

    RunnerPool(Deque<NamedPod> pool) {
      this.pool = pool;
    }

    Deque<NamedPod> getPool() {
      return this.pool;
    }

    NamedPod getNext() {
      return this.pool.pop();
    }

    boolean hasElements() {
      return !this.pool.isEmpty();
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
        String.format("Initialising StatefulSet with replicas := %d", EXPECTED_STATEFUL_SETS));
    labeledStatefulSet.setClient(kubernetesClient);
    StatefulSet statefulSet = labeledStatefulSet.blueprint();
    labeledStatefulSet.createStatefulSet(statefulSet);
    labeledStatefulSet.createService();
  }

  public Uni<NamedPod> getPod() {
    return getQueue()
        .onItem()
        .transform(Deque::pop);
  }


  public Uni<Deque<NamedPod>> getQueue() {
    Uni<AsyncMap<String, Deque<NamedPod>>> defaultMap = sharedData.getAsyncMap(POOL_NAME);

    return defaultMap
        .chain(map -> map.get(POOL_NAME))
        .replaceIfNullWith(newQueue()) // initialise queue if map has no queue
        .chain(queue -> {
          if (queue.isEmpty()) {
            return initialiseMapWithQueue(newQueue().get()); // reset queue if all pods were used
          } else {
            return Uni.createFrom().item(queue);
          }
        });
  }

  public Uni<Deque<NamedPod>> initialiseMapWithQueue(Deque<NamedPod> queue) {
    Uni<AsyncMap<String, Deque<NamedPod>>> asyncMap = sharedData.getAsyncMap(POOL_NAME);

    return asyncMap
        .onItem()
        .call(map -> map.put(POOL_NAME, queue))
        .replaceWith(() -> queue);
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

  public NamedPod getNextPod() {
    Uni<RunnerPool> supplier =
        sharedData.<String, RunnerPool>getAsyncMap(POOL_NAME).chain(fillQueue::apply);
    RunnerPool pool = applyWithActiveLock(supplier);
    return pool.getNext();
  }

  public Uni<NamedPod> getNextPodAsync() {
    return applyAsyncWithLock((asyncMap -> popPod.apply(asyncMap)));
  }

  public Uni<NamedPod> getStaticPod() {
    RunnerPool pool = newPool().get();
    NamedPod np = pool.getPool().getFirst();
    return Uni.createFrom().item(np);
  }

  public Uni<NamedPod> getPodAsync() {
    Uni<AsyncMap<String, RunnerPool>> asyncMap =
        sharedData.<String, RunnerPool>getAsyncMap(POOL_NAME);
    return asyncMap
        .onItem()
        .transform(map -> map.get(POOL_NAME))
        .chain(pool -> pool)
        .ifNoItem()
        .after(Duration.ofMillis(LOCK_TIMEOUT_MS))
        .recoverWithItem(newPool())
        .onItem()
        .transform(
            pool -> {
              Objects.requireNonNull(pool, "Pool is not allowed to be null");
              return pool.getPool().pop();
            });
  }

  <T> Uni<T> applyAsyncWithLock(
      Function<Uni<AsyncMap<String, RunnerPool>>, Uni<? extends T>> work) {
    Uni<Lock> lockUni = sharedData.getLock(POOL_NAME);
    Uni<AsyncMap<String, RunnerPool>> asyncMap =
        sharedData.<String, RunnerPool>getAsyncMap(POOL_NAME);
    Uni<T> uni = (Uni<T>) work.apply(asyncMap);
    return lockUni.chain(
        lock -> {
          lock.release();
          return uni;
        });
  }

  Function<Uni<AsyncMap<String, RunnerPool>>, Uni<RunnerPool>> accessRunnerPool =
      map -> map.flatMap(async -> async.get(POOL_NAME));

  Function<Uni<AsyncMap<String, RunnerPool>>, Uni<NamedPod>> popPod =
      map ->
          map.chain(inner -> inner.get(POOL_NAME))
              .ifNoItem()
              .after(Duration.ofMillis(LOCK_TIMEOUT_MS))
              .recoverWithItem(newPool())
              .onItem()
              .transform(
                  pool -> {
                    Objects.requireNonNull(pool, "Pool is not allowed to be null.");
                    return pool.getPool().pop();
                  });

  /**
   * This method is blocking on purpose. It is meant to act as a wrapper and the final call before
   * adjusting the pool.
   *
   * @param query of a given Uni to be returned
   * @param <T> return type of the given supplier most likely {@link NamedPod}.
   * @return after awaiting the asynchronous operation and releasing lock
   */
  <T> T applyWithActiveLock(Uni<T> query) {
    Lock lock = sharedData.getLockWithTimeoutAndAwait(POOL_NAME, LOCK_TIMEOUT_MS);
    T supplied = query.await().indefinitely();
    lock.release();
    return supplied;
  }

  Supplier<Deque<NamedPod>> newQueue() {
    return () -> {
      List<Pod> pods =
          kubernetesClient
              .pods()
              .inNamespace(DataCaterK8sConfig.NAMESPACE)
              .withLabels(DataCaterK8sConfig.LABELS)
              .list()
              .getItems();
      Deque<NamedPod> namedPods = new ArrayDeque<>(DataCaterK8sConfig.PYTHON_RUNNER_REPLICAS);
      for (Pod pod : pods) {
        String podName = pod.getMetadata().getName();
        namedPods.add(new NamedPod(podName, pod));
      }

      return namedPods;
    };
  }

  Supplier<RunnerPool> newPool() {
    return () -> {
      List<Pod> pods =
          kubernetesClient
              .pods()
              .inNamespace(DataCaterK8sConfig.NAMESPACE)
              .withLabels(DataCaterK8sConfig.LABELS)
              .list()
              .getItems();
      Deque<NamedPod> namedPods = new ArrayDeque<>(DataCaterK8sConfig.PYTHON_RUNNER_REPLICAS);
      for (Pod pod : pods) {
        String podName = pod.getMetadata().getName();
        namedPods.add(new NamedPod(podName, pod));
      }

      return new RunnerPool(namedPods);
    };
  }

  Function<AsyncMap<String, RunnerPool>, Uni<RunnerPool>> fillQueue =
      map -> {
        List<Pod> pods =
            kubernetesClient
                .pods()
                .inNamespace(DataCaterK8sConfig.NAMESPACE)
                .withLabels(DataCaterK8sConfig.LABELS)
                .list()
                .getItems();
        Deque<NamedPod> namedPods = new ArrayDeque<>(DataCaterK8sConfig.PYTHON_RUNNER_REPLICAS);
        for (Pod pod : pods) {
          String podName = pod.getMetadata().getName();
          namedPods.add(new NamedPod(podName, pod));
        }

        return map.get(POOL_NAME)
            .onItem()
            .ifNull()
            .continueWith(new RunnerPool(namedPods))
            .invoke(pool -> map.put(POOL_NAME, pool))
            .onItem()
            .ifNotNull()
            .invoke(pool -> map.replace(POOL_NAME, pool));
      };

  Function<Lock, Pod> lockTransformerUni =
      lock -> {
        Uni<AsyncMap<String, Pod>> pods = sharedData.getAsyncMap(POOL_NAME);
        lock.release();
        return pods.onItem().transform(locki -> new Pod()).await().indefinitely();
      };

  Function<AsyncMap<String, Deque<NamedPod>>, NamedPod> transformAsyncMapToPod =
      map ->
          map.get(POOL_NAME)
              .onItem()
              .transform(Deque::pop)
              .onFailure()
              .transform(
                  Unchecked.function(
                      throwable -> {
                        throw new DatacaterException(throwable.getMessage());
                      }))
              .await()
              .indefinitely();

  UnaryOperator<Pod> annotatePodAsUsed =
      pod -> {
        Map<String, String> annotations = pod.getMetadata().getAnnotations();
        String podName = pod.getMetadata().getName();

        if (annotations.containsKey(IN_USE_LABEL_KEY)
            && annotations.get(IN_USE_LABEL_KEY).equals(IN_USE_TRUE_LABEL_VALUE)) {
          throw new DatacaterException(
              String.format(
                  "Pod with Name := %s is annotated as in use. Will not re-annotate.", podName));
        }

        return kubernetesClient
            .pods()
            .inNamespace(DataCaterK8sConfig.NAMESPACE)
            .withName(podName)
            .edit(
                p ->
                    new PodBuilder(p)
                        .editOrNewMetadata()
                        .addToAnnotations(IN_USE_TRUE_LABEL)
                        .endMetadata()
                        .build());
      };
}

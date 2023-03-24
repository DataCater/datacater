package io.datacater.core.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.datacater.core.pipeline.Pipeline;
import io.datacater.core.pipeline.PipelineEntity;
import io.datacater.core.stream.Stream;
import io.datacater.core.stream.StreamEntity;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

@WithKubernetesTestServer
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class K8DeploymentTest {
  private static final Logger LOGGER = Logger.getLogger(K8DeploymentTest.class);

  @Inject KubernetesClient client;

  @KubernetesTestServer KubernetesServer mockServer;

  K8Deployment k8Deployment;
  PipelineEntity pipelineEntity;
  StreamEntity streamInEntity;
  StreamEntity streamOutEntity;

  UUID deploymentId;

  @BeforeAll
  void setUp() throws IOException {
    k8Deployment = new K8Deployment(client);
    ObjectMapper mapper = new JsonMapper();

    String streamInUUIDPlaceholder = "streaminUUIDPlaceholder";
    String streamOutUUIDPlaceholder = "streamoutUUIDPlaceholder";

    String streamInPath = "deploymentTests/streamin.json";
    String streamOutPath = "deploymentTests/streamout.json";
    String pipelinePath = "deploymentTests/pipeline.json";
    URL streamInUrl = ClassLoader.getSystemClassLoader().getResource(streamInPath);
    URL streamOutUrl = ClassLoader.getSystemClassLoader().getResource(streamOutPath);
    URL pipelineUrl = ClassLoader.getSystemClassLoader().getResource(pipelinePath);

    JsonNode pipelineJson = mapper.readTree(pipelineUrl);

    // create streams
    Stream streamIn = mapper.readValue(streamInUrl, Stream.class);
    Stream streamOut = mapper.readValue(streamOutUrl, Stream.class);
    streamInEntity = new StreamEntity(streamIn.name(), streamIn.spec());
    streamOutEntity = new StreamEntity(streamOut.name(), streamOut.spec());

    // create pipeline with random uuid. uuid isn't actually needed, but class can't be created
    // without them
    String pipelineString =
        pipelineJson.toString().replace(streamInUUIDPlaceholder, UUID.randomUUID().toString());
    pipelineString = pipelineString.replace(streamOutUUIDPlaceholder, UUID.randomUUID().toString());
    Pipeline pipeline = mapper.readValue(pipelineString, Pipeline.class);
    pipelineEntity = new PipelineEntity().updateEntity(pipeline);
  }

  @Test
  @Order(1)
  void testDeploymentExistsMethod()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Assertions.assertEquals(
        false, getDeploymentExistsMethod().invoke(k8Deployment, UUID.randomUUID()));
  }

  @Test
  @Order(1)
  void testConfigMapExistsMethod()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
          NoSuchFieldException {
    final String nonExistentConfigMap = "test" + UUID.randomUUID();
    Assertions.assertEquals(
        false,
        getConfigMapExistsMethod()
            .invoke(getPrivateK8ConfigMap().get(k8Deployment), nonExistentConfigMap));
  }

  @Test
  @Order(2)
  void testCreateDeployment()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
          JsonProcessingException {
    Stream streamIn = Stream.from(streamInEntity);
    Stream streamOut = Stream.from(streamOutEntity);
    deploymentId = UUID.randomUUID();
    k8Deployment.create(
        pipelineEntity, streamIn, streamOut, new DeploymentSpec("", new HashMap<>()), deploymentId);
    Assertions.assertEquals(true, getDeploymentExistsMethod().invoke(k8Deployment, deploymentId));
  }

  private Method getConfigMapExistsMethod() throws NoSuchMethodException {
    final String methodName = "exists";
    Method method = K8ConfigMap.class.getDeclaredMethod(methodName, String.class);
    method.setAccessible(true);
    return method;
  }

  private Method getDeploymentExistsMethod() throws NoSuchMethodException {
    final String methodName = "exists";
    Method method = K8Deployment.class.getDeclaredMethod(methodName, UUID.class);
    method.setAccessible(true);
    return method;
  }

  private Field getPrivateK8ConfigMap() throws NoSuchFieldException {
    Field nameField = k8Deployment.getClass().getDeclaredField("k8ConfigMap");
    nameField.setAccessible(true);
    return nameField;
  }
}

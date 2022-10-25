package io.datacater.core.deployment;

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
import java.util.UUID;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

@WithKubernetesTestServer
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class K8DeploymentTest {
  private static final Logger LOGGER = Logger.getLogger(K8DeploymentTest.class);

  @Inject KubernetesClient client;

  @KubernetesTestServer KubernetesServer mockServer;

  K8Deployment k8Deployment;
  DatacaterDeployment dcDeployment;
  PipelineEntity pipelineEntity;
  StreamEntity streamInEntity;
  StreamEntity streamOutEntity;

  String deploymentName;

  @BeforeAll
  void setUp() throws IOException {
    k8Deployment = new K8Deployment(client);
    ObjectMapper mapper = new JsonMapper();

    String streamInUUIDPlaceholder = "streaminUUIDPlaceholder";
    String streamOutUUIDPlaceholder = "streamoutUUIDPlaceholder";
    String pipelineUUIDPlaceholder = "pipelineUUIDPlaceholder";

    String streamInPath = "deploymentTests/streamin.json";
    String streamOutPath = "deploymentTests/streamout.json";
    String pipelinePath = "deploymentTests/pipeline.json";
    String deploymentPath = "deploymentTests/deployment-test-object.json";
    URL streamInUrl = ClassLoader.getSystemClassLoader().getResource(streamInPath);
    URL streamOutUrl = ClassLoader.getSystemClassLoader().getResource(streamOutPath);
    URL pipelineUrl = ClassLoader.getSystemClassLoader().getResource(pipelinePath);
    URL deploymentUrl = ClassLoader.getSystemClassLoader().getResource(deploymentPath);

    JsonNode pipelineJson = mapper.readTree(pipelineUrl);
    JsonNode deploymentJson = mapper.readTree(deploymentUrl);

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

    // create deployment
    String deploymentString =
        deploymentJson.toString().replace(pipelineUUIDPlaceholder, UUID.randomUUID().toString());
    dcDeployment = mapper.readValue(deploymentString, DatacaterDeployment.class);

    deploymentName = dcDeployment.name();
  }

  @Test
  @Order(1)
  public void testDeploymentExistsMethod()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    final String nonExistentDeployment = "test" + UUID.randomUUID();
    Assertions.assertEquals(
        false, getExistsMethod(K8Deployment.class).invoke(k8Deployment, nonExistentDeployment));
  }

  @Test
  @Order(1)
  public void testConfigMapExistsMethod()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
          NoSuchFieldException {
    final String nonExistentConfigMap = "test" + UUID.randomUUID();
    Assertions.assertEquals(
        false,
        getExistsMethod(K8ConfigMap.class)
            .invoke(getPrivateK8ConfigMap().get(k8Deployment), nonExistentConfigMap));
  }

  @Test
  @Order(2)
  public void testInteractionWithAPIServer()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    k8Deployment.create(dcDeployment, pipelineEntity, streamInEntity, streamOutEntity);
    Assertions.assertEquals(
        true, getExistsMethod(K8Deployment.class).invoke(k8Deployment, deploymentName));
  }

  @Test
  @Order(3)
  public void testGetLogs() {
    String logs = k8Deployment.getLogs(deploymentName);
    Assertions.assertNotNull(logs);
  }

  @Test
  @Order(4)
  public void testNameSpaceExists() {
    String logs = k8Deployment.getLogs(deploymentName);
    Assertions.assertNotNull(logs);
  }

  @Test
  @Order(5)
  public void testConfigMapExists() {
    String logs = k8Deployment.getLogs(deploymentName);
    Assertions.assertNotNull(logs);
  }

  private Method getExistsMethod(Class c) throws NoSuchMethodException {
    final String methodName = "exists";
    Method method = c.getDeclaredMethod(methodName, String.class);
    method.setAccessible(true);
    return method;
  }

  private Field getPrivateK8ConfigMap() throws NoSuchFieldException {
    Field nameField = k8Deployment.getClass().getDeclaredField("k8ConfigMap");
    nameField.setAccessible(true);
    return nameField;
  }
}

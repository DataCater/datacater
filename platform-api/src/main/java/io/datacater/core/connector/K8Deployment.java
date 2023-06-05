package io.datacater.core.connector;

import io.datacater.core.exceptions.*;
import io.datacater.core.stream.StreamEntity;
import io.datacater.core.utilities.StringUtilities;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ContainerResource;

import java.util.*;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

@Singleton
public class K8Deployment {
    private static final Logger LOGGER = Logger.getLogger(K8Deployment.class);
    private final KubernetesClient client;

    public K8Deployment(KubernetesClient client) {
        this.client = client;
    }

    public Map<String, Object> create(
            StreamEntity se, ConnectorSpec connectorSpec, UUID connectorId) {

        final String name = StaticConfig.CONNECTOR_NAME_PREFIX + connectorId;
        final int replicaCount = getDeploymentReplicaOrDefault(connectorSpec.connector());

        List<EnvVar> variables = getEnvironmentVariables(connectorSpec, connectorId);

        String connectorImage;
        try {
            connectorImage = connectorSpec.connector().get(StaticConfig.IMAGE_NODE_TEXT).toString();
        } catch (NullPointerException e) {
            throw new CreateConnectorException(StaticConfig.LoggerMessages.NO_IMAGE_PROVIDED);
        }

        try {
            Deployment deployment =
                    new DeploymentBuilder()
                            .withNewMetadata()
                            .withName(name)
                            .addToLabels(getLabels(connectorId, connectorSpec.name()))
                            .endMetadata()
                            .withNewSpec()
                            .withReplicas(replicaCount)
                            .withMinReadySeconds(StaticConfig.EnvironmentVariables.READY_SECONDS)
                            .withNewSelector()
                            .addToMatchLabels(getLabels(connectorId, connectorSpec.name()))
                            .endSelector()
                            .withNewTemplate()
                            .withNewMetadata()
                            .addToLabels(getLabels(connectorId, connectorSpec.name()))
                            .endMetadata()
                            .withNewSpec()
                            .addAllToContainers(
                                    List.of(
                                            connectorContainer(name, variables, connectorImage, se),
                                            conConSidecarContainer(connectorSpec, se)))
                            .endSpec()
                            .endTemplate()
                            .endSpec()
                            .build();

            var deploymentResource =
                    client.resource(deployment).inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE);
            deploymentResource.create();
        } catch (KubernetesClientException ex) {
            throw new CreateConnectorException(StringUtilities.wrapString(ex.getCause().getMessage()));
        }

        if (!exists(connectorId)) {
            throw new CreateConnectorException(StaticConfig.LoggerMessages.CONNECTOR_NOT_CREATED);
        }

        return getConnector(connectorId);
    }

    private static Map<String, String> getLabels(UUID connectorId, String prettyName) {
        return Map.of(
                StaticConfig.APP,
                StaticConfig.DATACATER_CONNECTOR,
                StaticConfig.CONNECTOR,
                StaticConfig.CONNECTOR_NO,
                StaticConfig.REVISION,
                StaticConfig.CONNECTOR_REV,
                StaticConfig.UUID_TEXT,
                connectorId.toString(),
                StaticConfig.CONNECTOR_NAME_TEXT,
                prettyName);
    }

    private Container conConSidecarContainer(ConnectorSpec connectorSpec, StreamEntity streamEntity) {
        List<EnvVar> envVariables = new ArrayList<>();
        envVariables.add(
                new EnvVarBuilder()
                        .withName("CONCON_KAFKA_CONNECT_URI")
                        .withValue("http://localhost:8083")
                        .build());
        envVariables.add(
                new EnvVarBuilder().withName("CONNECT_NAME").withValue(connectorSpec.name()).build());
        envVariables.add(
                new EnvVarBuilder()
                        .withName("CONNECT_CONFIG_TOPIC")
                        .withValue(streamEntity.getName())
                        .build());

        Map<String, String> config = (Map) connectorSpec.connector().getOrDefault("config", Map.of());

        for (String configName : config.keySet()) {
            envVariables.add(
                    new EnvVarBuilder()
                            .withName("CONNECT_CONFIG_" + configName.toUpperCase().replace(".", "_"))
                            .withValue(config.get(configName).toString())
                            .build());
        }

        return new ContainerBuilder(true)
                .withName(StaticConfig.CONCON_SIDECAR_NAME)
                .withImage(
                        String.format(
                                "%s:%s",
                                StaticConfig.EnvironmentVariables.CONCON_IMAGE_NAME,
                                StaticConfig.EnvironmentVariables.CONCON_IMAGE_TAG))
                .withImagePullPolicy(StaticConfig.EnvironmentVariables.PULL_POLICY)
                .withPorts(
                        new ContainerPortBuilder()
                                .withContainerPort(StaticConfig.EnvironmentVariables.CONCON_SIDECAR_HTTP_PORT)
                                .build())
                .withNewResources()
                .withRequests(StaticConfig.CONCON_SIDECAR_RESOURCE_REQUESTS)
                .withLimits(StaticConfig.CONCON_SIDECAR_RESOURCE_LIMITS)
                .endResources()
                .withEnv(envVariables)
                .build();
    }

    private Container connectorContainer(
            String name, List<EnvVar> variables, String image, StreamEntity streamEntity) {
        variables.add(
                new EnvVarBuilder()
                        .withName("HEAP_OPTS")
                        .withValue(StaticConfig.CONNECTOR_HEAP_OPTS)
                        .build());
        variables.add(new EnvVarBuilder().withName("GROUP_ID").withValue(name).build());
        variables.add(
                new EnvVarBuilder()
                        .withName("BOOTSTRAP_SERVERS")
                        .withValue(streamEntity.getSpec().get("kafka").get("bootstrap.servers").textValue())
                        .build());
        // TODO: allow to overwrite value via connector config
        variables.add(
                new EnvVarBuilder()
                        .withName("CONFIG_STORAGE_TOPIC")
                        .withValue("my_connect_configs")
                        .build());
        // TODO: allow to overwrite value via connector config
        variables.add(
                new EnvVarBuilder()
                        .withName("OFFSET_STORAGE_TOPIC")
                        .withValue("my_connect_offsets")
                        .build());
        // TODO: allow to overwrite value via connector config
        variables.add(
                new EnvVarBuilder()
                        .withName("STATUS_STORAGE_TOPIC")
                        .withValue("my_connect_statuses")
                        .build());

        return new ContainerBuilder(true)
                .withName(name)
                .withImage(image)
                .withImagePullPolicy(StaticConfig.EnvironmentVariables.PULL_POLICY)
                .withEnv(variables)
                .withPorts(
                        new ContainerPortBuilder()
                                .withContainerPort(StaticConfig.EnvironmentVariables.CONNECTOR_KAFKA_CONNECT_PORT)
                                .build())
                .withNewResources()
                .withRequests(StaticConfig.RESOURCE_REQUESTS)
                .withLimits(StaticConfig.RESOURCE_LIMITS)
                .endResources()
                .build();
    }

    public String getLogs(UUID connectorId, int tailingLines) {

        String connectorName = getDeploymentName(connectorId);
        Pod pod = getConnectorPod(connectorName);

        return client
                .pods()
                .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                .withName(pod.getMetadata().getName())
                .inContainer(StaticConfig.CONNECTOR_NAME_PREFIX + connectorId)
                .tailingLines(tailingLines)
                .getLog(true);
    }

    public ContainerResource watchLogs(UUID connectorId) {
        String connectorName = getDeploymentName(connectorId);
        Pod pod = getConnectorPod(connectorName);

        return client
                .pods()
                .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                .withName(pod.getMetadata().getName())
                .inContainer(StaticConfig.CONNECTOR_NAME_PREFIX + connectorId);
    }

    public void delete(UUID connectorId) {
        String name = getDeploymentName(connectorId);
        List<StatusDetails> status =
                client
                        .apps()
                        .deployments()
                        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                        .withName(name)
                        .delete();

        // Above request will return the deleted deployment in Kubernetes. We expect the matching
        // deployment to be exactly
        // one and continue only if that is true.
        if (status.size() == 1) {
            LOGGER.info(String.format(StaticConfig.LoggerMessages.CONNECTOR_DELETED, name));
        } else {
            LOGGER.info(String.format(StaticConfig.LoggerMessages.CONNECTOR_NOT_DELETED, name));
        }
    }

    private Map<String, Object> deploymentToMetaDataMap(Deployment deployment) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> node = new HashMap<>();
        if (deployment.getMetadata().getLabels() != null) {
            node.putAll(deployment.getMetadata().getLabels());
        }
        if (deployment.getMetadata().getAnnotations() != null) {
            node.putAll(deployment.getMetadata().getAnnotations());
        }
        if (deployment.getStatus() == null) {
            // return before trying to add Status MetaData
            map.put(deployment.getMetadata().getName(), node);
            return map;
        }

        if (deployment.getStatus().getAdditionalProperties() != null) {
            node.putAll(deployment.getStatus().getAdditionalProperties());
        }
        if (deployment.getStatus().getConditions() != null) {
            node.put(StaticConfig.CONDITIONS, deployment.getStatus().getConditions());
        }
        map.put(deployment.getMetadata().getName(), node);
        return map;
    }

    public Map<String, Object> getConnector(UUID connectorId) {
        try {
            return deploymentToMetaDataMap(
                    client
                            .apps()
                            .deployments()
                            .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                            .withName(getDeploymentName(connectorId))
                            .get());
        } catch (DeploymentNotFoundException ex) {
            Map<String, Object> errorMap = new HashMap<>();
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put(StaticConfig.MESSAGE_TAG, StaticConfig.LoggerMessages.K8_DEPLOYMENT_NOT_FOUND);
            errorMap.put(StaticConfig.ERROR_TAG, messageMap);
            return errorMap;
        }
    }

    public String getConnectorPodIp(UUID connectorId) {
        String connectorName = getDeploymentName(connectorId);
        Pod pod = getConnectorPod(connectorName);

        return pod.getStatus().getPodIP();
    }

    private boolean exists(UUID connectorId) {
        return !client
                .apps()
                .deployments()
                .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                .withLabel(StaticConfig.UUID_TEXT, connectorId.toString())
                .list()
                .getItems()
                .isEmpty();
    }

    public String getDeploymentName(UUID connectorId) {
        List<Deployment> deployments =
                client
                        .apps()
                        .deployments()
                        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                        .withLabel(StaticConfig.UUID_TEXT, connectorId.toString())
                        .list()
                        .getItems();
        if (deployments.isEmpty()) {
            throw new ConnectorNotFoundException(StaticConfig.LoggerMessages.CONNECTOR_NOT_FOUND);
        }
        return deployments.get(0).getMetadata().getName();
    }

    private List<EnvVar> getEnvironmentVariables(ConnectorSpec connectorSpec, UUID uuid) {
        List<EnvVar> environmentVariables = new ArrayList<>();

        // TODO

        return environmentVariables;
    }

    private int getDeploymentReplicaOrDefault(Map<String, Object> map) {
        int replica = StaticConfig.EnvironmentVariables.REPLICAS;

        return replica;
    }

    private Pod getConnectorPod(String connectorName) {
        final Map<String, String> matchLabels =
                client
                        .apps()
                        .deployments()
                        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                        .withName(connectorName)
                        .get()
                        .getSpec()
                        .getSelector()
                        .getMatchLabels();

        List<Pod> allConnectorPods =
                client
                        .pods()
                        .inNamespace(StaticConfig.EnvironmentVariables.NAMESPACE)
                        .withLabels(matchLabels)
                        .list()
                        .getItems();

        Pod searchedPod =
                allConnectorPods.stream()
                        .sorted((Comparator.comparing(o -> o.getMetadata().getName())))
                        .findFirst()
                        .orElseThrow(() -> new DatacaterException(
                                String.format(StaticConfig.LoggerMessages.NO_POD_FOUND, connectorName)));

        return searchedPod;
    }
}
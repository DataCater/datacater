package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import java.util.*;

public class DataCaterDeploymentStatus {
  @JsonProperty("availableReplicas")
  private Integer availableReplicas;

  @JsonProperty("collisionCount")
  private Integer collisionCount;

  /*
   DeploymentCondition is json serializable, but ignores additionalProperties. These are properties, which might occur
   due to changes in the K8s API. Mapping this class to one of our own seems like duplicating the inherent logic of
   DeploymentCondition.

   Reference:
   https://github.com/fabric8io/kubernetes-client/blob/master/kubernetes-model-generator/kubernetes-model-apps/src/generated/java/io/fabric8/kubernetes/api/model/apps/DeploymentCondition.java#L77
  */
  @JsonProperty("conditions")
  private List<DeploymentCondition> conditions;

  @JsonProperty("observedGeneration")
  private Long observedGeneration;

  @JsonProperty("readyReplicas")
  private Integer readyReplicas;

  @JsonProperty("replicas")
  private Integer replicas;

  @JsonProperty("unavailableReplicas")
  private Integer unavailableReplicas;

  @JsonProperty("updatedReplicas")
  private Integer updatedReplicas;

  @JsonProperty private Map<String, Object> additionalProperties;

  public static DataCaterDeploymentStatus from(Deployment deployment) {
    if (deployment == null) {
      return null;
    }

    DeploymentStatus deploymentStatus = deployment.getStatus();

    if (deploymentStatus == null) {
      return null;
    }

    return new DataCaterDeploymentStatus(deploymentStatus);
  }

  private DataCaterDeploymentStatus(DeploymentStatus deploymentStatus) {
    this.availableReplicas = Optional.ofNullable(deploymentStatus.getAvailableReplicas()).orElse(0);
    this.collisionCount = Optional.ofNullable(deploymentStatus.getCollisionCount()).orElse(0);
    this.conditions =
        Optional.ofNullable(deploymentStatus.getConditions()).orElse(new ArrayList<>());
    this.observedGeneration =
        Optional.ofNullable(deploymentStatus.getObservedGeneration()).orElse(0L);
    this.readyReplicas = Optional.ofNullable(deploymentStatus.getReadyReplicas()).orElse(0);
    this.replicas = Optional.ofNullable(deploymentStatus.getReplicas()).orElse(0);
    this.unavailableReplicas =
        Optional.ofNullable(deploymentStatus.getUnavailableReplicas()).orElse(0);
    this.updatedReplicas = Optional.ofNullable(deploymentStatus.getUpdatedReplicas()).orElse(0);
    this.additionalProperties =
        Optional.ofNullable(deploymentStatus.getAdditionalProperties()).orElse(new HashMap<>());
  }
}

package io.datacater.core.deployment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCaterDeploymentStatus {

  @JsonProperty("availableReplicas")
  private Integer availableReplicas;

  @JsonProperty("collisionCount")
  private Integer collisionCount;

  @JsonProperty("conditions")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<DeploymentCondition> conditions = new ArrayList<DeploymentCondition>();

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

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  public static DataCaterDeploymentStatus from(Deployment deployment) {
    if (deployment == null) {
      return null;
    }

    // getstatus returns null in tests
    DeploymentStatus deploymentStatus = deployment.getStatus();

    if (deploymentStatus == null) {
      return null;
    }

    DataCaterDeploymentStatus self = new DataCaterDeploymentStatus();

    // TODO add default values
    self.availableReplicas = deploymentStatus.getAvailableReplicas();
    self.collisionCount = deploymentStatus.getCollisionCount();
    self.conditions = deploymentStatus.getConditions();
    self.observedGeneration = deploymentStatus.getObservedGeneration();
    self.readyReplicas = deploymentStatus.getReadyReplicas();
    self.replicas = deploymentStatus.getReplicas();
    self.unavailableReplicas = deploymentStatus.getUnavailableReplicas();
    self.updatedReplicas = deploymentStatus.getUpdatedReplicas();
    self.additionalProperties = deploymentStatus.getAdditionalProperties();

    return self;
  }

  private DataCaterDeploymentStatus() {}
}

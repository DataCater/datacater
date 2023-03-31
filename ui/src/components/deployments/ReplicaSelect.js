import React, { Component } from "react";
import Select from "react-select";

class ReplicaSelect extends Component {
  render() {
    const { currentReplica, deployment, updateCurrentReplicaFunc } = this.props;

    if (
      deployment === undefined ||
      deployment.spec === undefined ||
      deployment.spec.replicas < 1
    ) {
      return (
        <button className="btn btn-light ms-2" disabled={true}>
          No replicas available
        </button>
      );
    }

    const replicas =
      deployment.spec === undefined || isNaN(parseInt(deployment.spec.replicas))
        ? 1 // By default, deployments have 1 replica
        : parseInt(deployment.spec.replicas);

    const replicaOptions = [];
    for (let i = 0; i < replicas; i++) {
      const replica = i + 1;
      replicaOptions.push({
        label: `Replica ${replica}`,
        value: replica,
      });
    }

    return (
      <div className="float-end ms-2">
        <Select
          isSearchable
          defaultValue={
            replicaOptions.filter(
              (option) => option.value === currentReplica
            )[0]
          }
          options={replicaOptions}
          onChange={(value) => updateCurrentReplicaFunc(value.value)}
          styles={{
            control: (baseStyles, state) => ({
              ...baseStyles,
              borderRadius: "0.375rem",
            }),
          }}
        />
      </div>
    );
  }
}

export default ReplicaSelect;

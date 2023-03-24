import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { addDeployment } from "../../actions/deployments";
import { fetchPipelines } from "../../actions/pipelines";
import "../../scss/fonts.scss";

class NewDeployment extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingDeploymentFailed: false,
      errorMessages: {},
      deployment: {
        spec: {},
        configSelector: {},
      },
      tempLabel: {
        labelKey: "",
        labelValue: "",
      },
      deploymentCreated: false,
    };

    this.handleCreateDeployment = this.handleCreateDeployment.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleEventChange = this.handleEventChange.bind(this);
    this.addLabel = this.addLabel.bind(this);
    this.removeLabel = this.removeLabel.bind(this);
    this.updateTempLabel = this.updateTempLabel.bind(this);
  }

  componentDidMount() {
    this.props.fetchPipelines();
  }

  handleCreateDeployment(event) {
    event.preventDefault();

    this.props.addDeployment(this.state.deployment).then(() => {
      if (this.props.deployments.errorMessage !== undefined) {
        this.setState({
          deploymentCreated: false,
          errorMessage: this.props.deployments.errorMessage,
        });
      } else {
        this.setState({
          deploymentCreated: true,
          errorMessage: "",
        });
      }
    });
  }

  handleChange(name, value, prefix) {
    let deployment = this.state.deployment;

    if (prefix === "metadata") {
      deployment.metadata[name] = value;
    } else if (prefix === "spec") {
      deployment.spec[name] = value;
    } else if (prefix === "spec.replicas") {
      if (!isNaN(value)) {
        deployment.spec[name] = parseInt(value);
      }
    } else if (prefix === "configSelector") {
      deployment.configSelector[name] = value;
    } else {
      deployment[name] = value;
    }

    this.setState({
      creatingDeploymentFailed: false,
      errorMessage: "",
      deployment: deployment,
    });
  }

  handleEventChange(event) {
    let deployment = this.state.deployment;

    const newValue =
      event.target.type === "checkbox"
        ? event.target.checked
        : event.target.value;

    switch (event.target.dataset.prefix) {
      case "metadata":
        deployment.metadata[event.target.name] = newValue;
        break;
      case "spec":
        deployment.spec[event.target.name] = newValue;
        break;
      case "configSelector":
        deployment.configSelector[event.target.name] = newValue;
        break;
      default:
        deployment[event.target.name] = newValue;
        break;
    }

    this.setState({
      creatingDeploymentFailed: false,
      errorMessage: "",
      deployment: deployment,
    });
  }

  updateTempLabel(field, value) {
    let tempLabel = this.state.tempLabel;
    tempLabel[field] = value;
    this.setState({ tempLabel: tempLabel });
  }

  addLabel(event) {
    event.preventDefault();
    const tempLabel = this.state.tempLabel;
    let deployment = this.state.deployment;
    deployment.configSelector[tempLabel.labelKey] = tempLabel.labelValue;
    this.setState({
      deployment: deployment,
      tempLabel: {
        labelKey: "",
        labelValue: "",
      },
    });
  }

  removeLabel(event) {
    event.preventDefault();
    let deployment = this.state.deployment;
    const label = event.target.dataset.label;
    delete deployment.configSelector[label];
    this.setState({ deployment: deployment });
  }

  render() {
    if (this.state.deploymentCreated) {
      return (
        <Redirect
          to={"/deployments/" + this.props.deployments.deployment.uuid}
        />
      );
    }

    const deployment = this.state.deployment;
    const addedLabels = Object.keys(deployment.configSelector);
    const pipelineOptions = this.props.pipelines.pipelines.map((pipeline) => {
      const name = `${pipeline.name || "Untitled pipeline"} (${pipeline.uuid})`;
      return { value: pipeline.uuid, label: name };
    });

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Deployments", uri: "/deployments" },
              { name: "New deployment" },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/deployments/"
            apiPath="/deployments/"
            httpMethod="POST"
            requestBody={this.state.deployment}
            title="Create new deployment"
            subTitle="Deployments operate Pipelines."
          />
          <form>
            <div className="col-12 mt-4">
              <label htmlFor="name" className="form-label h5 fw-semibold mb-3">
                Name
              </label>
              <input
                type="text"
                className="form-control"
                id="name"
                name="name"
                onChange={(event) => {
                  this.handleChange("name", event.target.value);
                }}
                value={this.state.deployment["name"] || ""}
              />
            </div>
            <div className="col-12 mt-4">
              <label className="form-label h5 fw-semibold mb-3">Pipeline</label>
              <Select
                isSearchable
                isClearable
                options={pipelineOptions}
                onChange={(value) => {
                  this.handleChange("pipeline", value.value, "spec");
                }}
              />
            </div>
            <div className="col-12 mt-4">
              <label className="form-label h5 fw-semibold mb-3">Replicas</label>
              <input
                type="text"
                className="form-control"
                id="replicas"
                name="replicas"
                onChange={(value) => {
                  this.handleChange(
                    "replicas",
                    value.target.value,
                    "spec.replicas"
                  );
                }}
                placeholder="1"
                value={this.state.deployment.spec["replicas"] || ""}
              />
            </div>
            <div className="col-12 mt-4">
              <h5 className="d-inline me-2 fw-semibold">Config selector</h5>
              <span className="text-muted fs-7">
                You can reference one or multiple Configs by their key and
                value.
              </span>
            </div>
            {addedLabels.length === 0 && (
              <div className="col-12 mt-2 mb-n1">
                <i>No configs referenced.</i>
              </div>
            )}
            {addedLabels.map((label) => (
              <div className="col-12 mt-2" key={label}>
                <label htmlFor={label} className="form-label">
                  Key: {label}
                  <a
                    className="ms-2 fs-7"
                    data-label={label}
                    data-prefix="configSelector"
                    href="/deployments/new"
                    onClick={this.removeLabel}
                  >
                    Remove config selector
                  </a>
                </label>
                <input
                  type="text"
                  className="form-control"
                  id={label}
                  data-prefix="configSelector"
                  name={label}
                  onChange={this.handleEventChange}
                  value={this.state.deployment.configSelector[label] || ""}
                />
              </div>
            ))}
            <div className="col-12 mt-3">
              <div className="row">
                <div className="col-md-3">
                  <label className="form-label">Key</label>
                  <input
                    type="text"
                    className="form-control"
                    name="labelKey"
                    onChange={(event) => {
                      this.updateTempLabel("labelKey", event.target.value);
                    }}
                    value={this.state.tempLabel.labelKey || ""}
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Value</label>
                  <input
                    type="text"
                    className="form-control"
                    name="labelValue"
                    onChange={(event) => {
                      this.updateTempLabel("labelValue", event.target.value);
                    }}
                    value={this.state.tempLabel.labelValue || ""}
                  />
                </div>
                <div className="col-md-3 d-flex align-items-end">
                  <a
                    href="/deployments/new"
                    className="btn btn-outline-primary"
                    data-prefix="configSelector"
                    onClick={this.addLabel}
                  >
                    Add
                  </a>
                </div>
              </div>
            </div>
            {![undefined, ""].includes(this.state.errorMessage) && (
              <div className="alert alert-danger mt-4">
                <p className="h6 fs-bolder">API response:</p>
                {this.state.errorMessage}
              </div>
            )}
            <div className="col-12 mt-4">
              <a
                href="/deployments/new"
                className="btn btn-primary text-white mb-4"
                onClick={this.handleCreateDeployment}
              >
                Create deployment
              </a>
            </div>
          </form>
        </div>
      </div>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    deployments: state.deployments,
    pipelines: state.pipelines,
  };
};

const mapDispatchToProps = {
  addDeployment: addDeployment,
  fetchPipelines: fetchPipelines,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewDeployment);

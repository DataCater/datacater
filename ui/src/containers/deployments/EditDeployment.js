import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { fetchDeployment, updateDeployment } from "../../actions/deployments";
import { fetchPipelines } from "../../actions/pipelines";
import "../../scss/fonts.scss";

class EditDeployment extends Component {
  constructor(props) {
    super(props);

    this.state = {
      updatingDeploymentFailed: false,
      errorMessages: {},
      deployment: undefined,
      deploymentUpdated: false,
    };

    this.handleUpdateDeployment = this.handleUpdateDeployment.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  componentDidMount() {
    this.props
      .fetchDeployment(this.getDeploymentId())
      .then(() =>
        this.setState({ deployment: this.props.deployments.deployment })
      );
    this.props.fetchPipelines();
  }

  getDeploymentId() {
    return this.props.match.params.id;
  }

  handleUpdateDeployment(event) {
    event.preventDefault();

    this.props
      .updateDeployment(this.getDeploymentId(), this.state.deployment)
      .then(() => {
        if (this.props.deployments.errorMessage !== undefined) {
          this.setState({
            deploymentUpdated: false,
            errorMessage: this.props.deployments.errorMessage,
          });
        } else {
          this.setState({
            deploymentUpdated: true,
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
    } else {
      deployment[name] = value;
    }

    this.setState({
      updatingDeploymentFailed: false,
      errorMessage: "",
      deployment: deployment,
    });
  }

  render() {
    if (this.state.deploymentUpdated) {
      return <Redirect to={`/deployments/${this.getDeploymentId()}`} />;
    }

    const deployment = this.state.deployment;

    if (deployment === undefined) {
      return <></>;
    }

    const apiPayload = Object.assign({}, deployment);
    delete apiPayload.uuid;
    delete apiPayload.createdAt;
    delete apiPayload.updatedAt;
    delete apiPayload.status;

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
              { name: deployment.uuid, uri: `/deployments/${deployment.uuid}` },
              { name: "Edit" },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/deployments/"
            apiPath={`/deployments/${deployment.uuid}`}
            httpMethod="PUT"
            requestBody={apiPayload}
            title={deployment.name || "Untitled deployment"}
          />
          <form>
            <div className="col-12 mt-4">
              <label htmlFor="name" className="form-label">
                Name{" "}
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
              <label className="form-label">Pipeline</label>
              <Select
                isSearchable
                isClearable
                defaultValue={
                  pipelineOptions.filter(
                    (option) => option.value === deployment.spec["pipeline"]
                  )[0]
                }
                options={pipelineOptions}
                onChange={(value) => {
                  this.handleChange("pipeline", value.value, "spec");
                }}
              />
            </div>
            {![undefined, ""].includes(this.state.errorMessage) && (
              <div className="alert alert-danger mt-4">
                <p className="h6 fs-bolder">API response:</p>
                {this.state.errorMessage}
              </div>
            )}
            <div className="col-12 mt-4">
              <a
                href={`/deployments/${deployment.uuid}`}
                className="btn btn-primary text-white mb-4"
                onClick={this.handleUpdateDeployment}
              >
                Update deployment
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
  fetchDeployment: fetchDeployment,
  updateDeployment: updateDeployment,
  fetchPipelines: fetchPipelines,
};

export default connect(mapStateToProps, mapDispatchToProps)(EditDeployment);

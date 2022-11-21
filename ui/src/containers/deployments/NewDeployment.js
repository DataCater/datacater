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
      },
      deploymentCreated: false,
    };

    this.handleCreateDeployment = this.handleCreateDeployment.bind(this);
    this.handleChange = this.handleChange.bind(this);
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
    } else {
      deployment[name] = value;
    }

    this.setState({
      creatingDeploymentFailed: false,
      errorMessage: "",
      deployment: deployment,
    });
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
              <label htmlFor="name" className="form-label">
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
              <label className="form-label">Pipeline</label>
              <Select
                isSearchable
                isClearable
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

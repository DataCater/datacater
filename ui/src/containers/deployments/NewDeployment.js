import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import { Copy } from "react-feather";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { addDeployment } from "../../actions/deployments";
import { fetchPipelines } from "../../actions/pipelines";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import "../../scss/fonts.scss";

class NewDeployment extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingDeploymentFailed: false,
      errorMessages: {},
      showApiCall: false,
      deployment: {
        metadata: {},
        spec: {},
      },
      deploymentCreated: false,
    };

    this.handleCreateDeployment = this.handleCreateDeployment.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
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

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
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
          <div className="col-12 mt-3">
            <div
              className="card welcome-card py-2"
              style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
            >
              <div className="card-body text-center p-0">
                <div className="row justify-content-center">
                  <div className="col-10 text-start">
                    <h4 className="fw-semibold mb-0">Create new deployment</h4>
                    <p className="text-white mb-0">
                      Deployments operate Pipelines.
                    </p>
                  </div>
                  <div className="col-2 d-flex align-items-center justify-content-end">
                    <a
                      href="/deployments/new"
                      className="btn btn-light btn-pill"
                      onClick={this.toggleShowApiCall}
                    >
                      {this.state.showApiCall ? "Hide" : "Show"} API call
                    </a>
                  </div>
                </div>
                {this.state.showApiCall && (
                  <div className="bg-black mx-n3 p-3 mt-3 mb-n3 text-start">
                    <pre className="mb-0">
                      <a
                        href="https://docs.datacater.io/docs/api/deployments/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light float-end"
                      >
                        See docs
                      </a>
                      <a
                        href="/deployments/new/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light me-2 float-end"
                        onClick={(e) => {
                          e.preventDefault();
                          navigator.clipboard.writeText(
                            "curl " +
                              getApiPathPrefix(true) +
                              "/deployments -XPOST -H'Content-Type:application/json' -H'Authorization:Bearer YOUR_TOKEN' -d'" +
                              JSON.stringify(this.state.deployment) +
                              "'"
                          );
                        }}
                      >
                        <Copy className="feather-icon" />
                      </a>
                      <code className="text-white">
                        $ curl {getApiPathPrefix(true)}/deployments/ \<br />
                        <span className="me-2"></span> -XPOST \<br />
                        <span className="me-2"></span>{" "}
                        -H&apos;Authorization:Bearer YOUR_TOKEN&apos; \<br />
                        <span className="me-2"></span>{" "}
                        -H&apos;Content-Type:application/json&apos; \<br />
                        <span className="me-2"></span> -d&apos;
                        {JSON.stringify(this.state.deployment)}&apos;
                      </code>
                    </pre>
                  </div>
                )}
              </div>
            </div>
          </div>
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

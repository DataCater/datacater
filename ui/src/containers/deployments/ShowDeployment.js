import React, { Component } from "react";
import { connect } from "react-redux";
import { Copy, Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import * as YAML from "json-to-pretty-yaml";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import { deleteDeployment, fetchDeployment } from "../../actions/deployments";

class ShowDeployment extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showApiCall: false,
      deploymentDeleted: false,
    };
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
    this.handleDelete = this.handleDelete.bind(this);
  }

  componentDidMount() {
    this.props.fetchDeployment(this.getDeploymentId());
  }

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
    });
  }

  getDeploymentId() {
    return this.props.match.params.id;
  }

  handleDelete(event) {
    event.preventDefault();

    if (
      window.confirm("Are you sure that you want to delete the deployment?")
    ) {
      this.props.deleteDeployment(this.getDeploymentId()).then(() => {
        this.setState({ deploymentDeleted: true });
      });
    }
  }

  render() {
    if (this.state.deploymentDeleted) {
      return <Redirect to="/deployments" />;
    }

    const deployment = this.props.deployments.deployment;

    if (![undefined, ""].includes(this.props.deployments.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.deployments.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    if (deployment === undefined) {
      return <div></div>;
    }

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Deployments", uri: "/deployments" },
              { name: deployment.uuid },
            ]}
          />
          <div className="col-12 mt-3">
            <div
              className="card welcome-card py-2"
              style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
            >
              <div className="card-body text-center p-0">
                <div className="row justify-content-center">
                  <div className="col-6 text-start d-flex align-items-center">
                    <h4 className="fw-semibold mb-0">
                      {deployment.name || "Untitled deployment"}
                    </h4>
                  </div>
                  <div className="col-6 d-flex align-items-center justify-content-end">
                    <div>
                      <a
                        href="/deployments"
                        className="btn btn-light btn-pill"
                        onClick={this.toggleShowApiCall}
                      >
                        {this.state.showApiCall ? "Hide" : "Show"} API call
                      </a>
                      <a
                        href={`/deployments/${deployment.uuid}/edit`}
                        className="btn btn-primary text-white ms-2"
                      >
                        Edit
                      </a>
                      <a
                        href={`/deployments/${deployment.uuid}/logs`}
                        className="btn btn-light ms-2"
                      >
                        Logs
                      </a>
                      <a
                        href={`/deployments/${deployment.uuid}`}
                        onClick={this.handleDelete}
                        className="btn btn-light btn-outline-danger ms-2"
                      >
                        <Trash2 className="feather-icon" />
                      </a>
                    </div>
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
                        href="/deployments/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light me-2 float-end"
                        onClick={(e) => {
                          e.preventDefault();
                          navigator.clipboard.writeText(
                            "curl " +
                              getApiPathPrefix(true) +
                              "/deployments/" +
                              deployment.uuid +
                              " -H'Authorization:Bearer YOUR_TOKEN'"
                          );
                        }}
                      >
                        <Copy className="feather-icon" />
                      </a>
                      <code className="text-white">
                        $ curl {getApiPathPrefix(true)}/deployments/
                        {stream.uuid} \
                        <br />
                        <span className="me-2"></span>{" "}
                        -H&apos;Authorization:Bearer YOUR_TOKEN&apos;
                        <br />
                      </code>
                    </pre>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
        <div className="row mt-4">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <SyntaxHighlighter
                  language="yaml"
                  showLineNumbers={true}
                  showInlineLineNumbers={true}
                  customStyle={{ marginBottom: "0px", background: "none" }}
                >
                  {YAML.stringify(deployment)}
                </SyntaxHighlighter>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    deployments: state.deployments,
  };
};

const mapDispatchToProps = {
  deleteDeployment: deleteDeployment,
  fetchDeployment: fetchDeployment,
};

export default connect(mapStateToProps, mapDispatchToProps)(ShowDeployment);

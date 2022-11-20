import React, { Component } from "react";
import { connect } from "react-redux";
import { Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import * as YAML from "json-to-pretty-yaml";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { deleteDeployment, fetchDeployment } from "../../actions/deployments";

class ShowDeployment extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deploymentDeleted: false,
    };
    this.handleDelete = this.handleDelete.bind(this);
  }

  componentDidMount() {
    this.props.fetchDeployment(this.getDeploymentId());
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
          <Header
            apiDocs="https://docs.datacater.io/docs/api/deployments/"
            apiPath={`/deployments/${deployment.uuid}`}
            buttons={
              <>
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
              </>
            }
            title={deployment.name || "Untitled deployment"}
          />
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

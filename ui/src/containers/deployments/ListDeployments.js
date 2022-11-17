import React, { Component } from "react";
import { connect } from "react-redux";
import { Copy } from "react-feather";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import { fetchDeployments } from "../../actions/deployments";

class ListDeployments extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showApiCall: false,
    };
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
  }

  componentDidMount() {
    this.props.fetchDeployments();
  }

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
    });
  }

  render() {
    const deployments = this.props.deployments.deployments;

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

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb items={[{ name: "Deployments" }]} />
          <div className="col-12 mt-3">
            <div
              className="card welcome-card py-2"
              style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
            >
              <div className="card-body text-center p-0">
                <div className="row justify-content-center">
                  <div className="col-8 text-start">
                    <h4 className="fw-semibold mb-0">Deployments</h4>
                    <p className="text-white mb-0">
                      Deployments operate Pipelines.
                    </p>
                  </div>
                  <div className="col-4 d-flex align-items-center justify-content-end">
                    <div>
                      <a
                        href="/deployments"
                        className="btn btn-light btn-pill"
                        onClick={this.toggleShowApiCall}
                      >
                        {this.state.showApiCall ? "Hide" : "Show"} API call
                      </a>
                      {deployments.length > 0 && (
                        <a
                          href="/deployments/new"
                          className="btn btn-primary text-white ms-2"
                        >
                          Create new deployment
                        </a>
                      )}
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
                              "/deployments -H'Authorization:Bearer YOUR_TOKEN'"
                          );
                        }}
                      >
                        <Copy className="feather-icon" />
                      </a>
                      <code className="text-white">
                        $ curl {getApiPathPrefix(true)}/deployments/ \<br />
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
            {deployments.length === 0 && (
              <div className="card">
                <div className="card-body">
                  <div className="d-flex align-items-center justify-content-center m-5">
                    <a
                      href="/deployments/new"
                      className="btn btn-lg text-white"
                      style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
                    >
                      Create your first deployment
                    </a>
                  </div>
                </div>
              </div>
            )}
            {deployments.length > 0 && (
              <div className="list-group">
                {deployments.map((deployment) => (
                  <a
                    href={`/deployments/${deployment.uuid}`}
                    key={deployment.uuid}
                    className="list-group-item list-group-item-action bg-white p-4"
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h5 className="d-flex align-items-center">
                        {deployment.name || "Untitled deployment"}
                      </h5>
                      <small className="d-flex align-items-center">
                        {deployment.uuid}
                      </small>
                    </div>
                  </a>
                ))}
              </div>
            )}
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
  fetchDeployments: fetchDeployments,
};

export default connect(mapStateToProps, mapDispatchToProps)(ListDeployments);

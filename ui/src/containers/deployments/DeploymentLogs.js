import React, { Component } from "react";
import { connect } from "react-redux";
import { Copy, Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import {
  fetchDeployment,
  fetchDeploymentLogs,
} from "../../actions/deployments";

class DeploymentLogs extends Component {
  constructor(props) {
    super(props);
    this.state = {
      followLogs: false,
      showApiCall: false,
      logMessages: [],
      wrapLines: false,
    };
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
    this.fetchLogs = this.fetchLogs.bind(this);
    this.toggleFollowLogs = this.toggleFollowLogs.bind(this);
    this.toggleWrapLines = this.toggleWrapLines.bind(this);
  }

  componentDidMount() {
    this.props.fetchDeployment(this.getDeploymentId());
    this.fetchLogs();
  }

  componentWillUnmount() {
    clearInterval(this.followLogsIntervalId);
  }

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
    });
  }

  fetchLogs() {
    this.props.fetchDeploymentLogs(this.getDeploymentId()).then(() => {
      this.setState({
        logMessages: this.props.deployments.logMessages,
      });
    });
  }

  toggleFollowLogs(event) {
    event.preventDefault();

    // TODO: Remove polling of the API endpoint
    // once we can make use of server-sent events here
    if (this.state.followLogs) {
      // Stop following logs
      clearInterval(this.followLogsIntervalId);
    } else {
      // Start following logs
      this.followLogsIntervalId = setInterval(this.fetchLogs, 2000);
    }

    this.setState({
      followLogs: !this.state.followLogs,
    });
  }

  toggleWrapLines(event) {
    event.preventDefault();

    this.setState({
      wrapLines: !this.state.wrapLines,
    });
  }

  getDeploymentId() {
    return this.props.match.params.id;
  }

  render() {
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

    const maxLogScreenHeight = window.innerHeight - 320;
    const followLogsClassNames = this.state.followLogs
      ? "btn btn-sm btn-primary text-white"
      : "btn btn-sm btn-outline-primary";
    const wrapLinesClassNames = this.state.wrapLines
      ? "btn btn-sm btn-primary text-white ms-2"
      : "btn btn-sm btn-outline-primary ms-2";
    const whitespaceWrap = this.state.wrapLines ? "normal" : "nowrap";

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Deployments", uri: "/deployments" },
              { name: deployment.uuid, uri: `/deployments/${deployment.uuid}` },
              { name: "Logs" },
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
                        {deployment.uuid}/logs \
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
            {!this.props.deployments.fetchingLogMessages &&
              (this.state.logMessages === undefined ||
                this.state.logMessages.length === 0) && (
                <div className="alert alert-warning" role="alert">
                  We couldn&apos;t read any log messages.
                </div>
              )}
            {this.state.logMessages !== undefined &&
              this.state.logMessages.length > 0 && (
                <>
                  <div className="mb-2">
                    <a
                      href={`/deployments/${deployment.uuid}/logs`}
                      onClick={this.toggleFollowLogs}
                      className={followLogsClassNames}
                    >
                      Follow logs
                    </a>
                    <a
                      href={`/deployments/${deployment.uuid}/logs`}
                      onClick={this.toggleWrapLines}
                      className={wrapLinesClassNames}
                    >
                      Wrap lines
                    </a>
                  </div>
                  <div className="card">
                    <div className="card-body bg-dark p-0">
                      <code
                        className="bg-dark text-white d-block p-2 ps-3 pt-3"
                        style={{
                          fontFamily: "Roboto Mono,monospace",
                          maxHeight: maxLogScreenHeight,
                          overflow: "scroll",
                          whiteSpace: whitespaceWrap,
                        }}
                      >
                        {this.state.logMessages.map((logMessage, idx) => (
                          <React.Fragment key={idx}>
                            <span className="me-2" style={{ color: "#ae75e6" }}>
                              {logMessage.timestamp}
                            </span>
                            <span className="me-2" style={{ color: "#b4d2ea" }}>
                              {logMessage.loggerName}
                            </span>
                            {logMessage.message} <br />
                          </React.Fragment>
                        ))}
                      </code>
                    </div>
                  </div>
                </>
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
  fetchDeployment: fetchDeployment,
  fetchDeploymentLogs: fetchDeploymentLogs,
};

export default connect(mapStateToProps, mapDispatchToProps)(DeploymentLogs);

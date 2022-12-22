import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import {
  fetchDeployment,
  fetchDeploymentLogs,
} from "../../actions/deployments";

class DeploymentLogs extends Component {
  constructor(props) {
    super(props);
    this.state = {
      followLogs: false,
      logMessages: [],
      wrapLines: false,
    };
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
          <Header
            apiDocs="https://docs.datacater.io/docs/api/deployments/"
            apiPath={`/deployments/${deployment.uuid}/logs`}
            title={deployment.name || "Untitled deployment"}
          />
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
                            {typeof logMessage === "string" && (
                              <>
                                {logMessage} <br />
                              </>
                            )}
                            {typeof logMessage !== "string" && (
                              <>
                                <span
                                  className="me-2"
                                  style={{ color: "#ae75e6" }}
                                >
                                  {logMessage.timestamp}
                                </span>
                                <span
                                  className="me-2"
                                  style={{ color: "#b4d2ea" }}
                                >
                                  {logMessage.loggerName}
                                </span>
                                {logMessage.message} <br />
                              </>
                            )}
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

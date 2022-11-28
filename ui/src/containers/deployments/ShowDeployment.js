import React, { Component } from "react";
import { connect } from "react-redux";
import { Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import * as YAML from "json-to-pretty-yaml";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import parsePrometheusTextFormat from "parse-prometheus-text-format";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import ApiCall from "../../components/layout/ApiCall";
import {
  deleteDeployment,
  fetchDeployment,
  fetchDeploymentHealth,
  fetchDeploymentMetrics,
} from "../../actions/deployments";

class ShowDeployment extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deploymentDeleted: false,
      showHealthEndpoint: false,
      showMetricsEndpoint: false,
    };
    this.handleDelete = this.handleDelete.bind(this);
    this.toggleHealthEndpoint = this.toggleHealthEndpoint.bind(this);
    this.toggleMetricsEndpoint = this.toggleMetricsEndpoint.bind(this);
  }

  componentDidMount() {
    this.props.fetchDeployment(this.getDeploymentId());
    this.props.fetchDeploymentHealth(this.getDeploymentId());
    this.props.fetchDeploymentMetrics(this.getDeploymentId());
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

  toggleHealthEndpoint() {
    this.setState({
      showHealthEndpoint: !this.state.showHealthEndpoint,
    });
  }

  toggleMetricsEndpoint() {
    this.setState({
      showMetricsEndpoint: !this.state.showMetricsEndpoint,
    });
  }

  preProcessPrometheusMetrics(prometheusMetrics, metricsOfInterest) {
    if (prometheusMetrics === undefined) {
      return "";
    }

    return prometheusMetrics
      .split("\n")
      .filter((line) =>
        metricsOfInterest.some((metric) => line.startsWith(metric.name))
      )
      .join("\n");
  }

  formatMetric(metric) {
    if (isNaN(metric)) {
      return metric;
    } else {
      const parsedMetric = parseInt(metric);

      if (parsedMetric < 1024) {
        return parsedMetric;
      }
    }
  }

  renderMetric(metric, metrics) {
    const prometheusMetric = metrics.find(
      (entry) => entry.name === metric.name
    );

    return (
      <div className="col-12 col-md-3 col-lg-2 mb-4" key={metric.label}>
        <div className="card">
          <div className="card-body">
            <p className="h5 fw-bold text-center">{metric.label}</p>
            <p className="h6 text-center text-muted">{metric.sublabel}</p>
            <p className="h4 mt-2 mb-0 text-center">
              {prometheusMetric &&
                this.formatMetric(prometheusMetric.metrics[0].value)}
              {!prometheusMetric && "n/a"}
            </p>
          </div>
        </div>
      </div>
    );
  }

  render() {
    if (this.state.deploymentDeleted) {
      return <Redirect to="/deployments" />;
    }

    const metricsOfInterest = [
      {
        name: "kafka_consumer_fetch_manager_bytes_consumed_rate",
        label: "Bytes in",
        sublabel: "per sec",
      },
      {
        name: "kafka_consumer_fetch_manager_records_consumed_rate",
        label: "Records in",
        sublabel: "per sec",
      },
      {
        name: "kafka_producer_outgoing_byte_rate",
        label: "Bytes out",
        sublabel: "per sec",
      },
      {
        name: "kafka_producer_record_send_rate",
        label: "Records out",
        sublabel: "per sec",
      },
      {
        name: "kafka_consumer_fetch_manager_records_lag_max",
        label: "Max lag",
        sublabel: "per partition",
      },
    ];

    const deployment = this.props.deployments.deployment;
    const health = this.props.deployments.health;
    const metrics = parsePrometheusTextFormat(
      this.preProcessPrometheusMetrics(
        this.props.deployments.metrics,
        metricsOfInterest
      )
    );

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
          {health !== undefined && health.status === "UP" && (
            <div className="col-12">
              <div className="alert alert-primary mb-0 rounded-0">
                <div className="row align-items-center">
                  <div className="col">
                    <b>Deployment is healthy</b>
                  </div>
                  <div className="col-auto">
                    <button
                      className="btn btn-sm btn-outline-primary ms-3"
                      onClick={this.toggleHealthEndpoint}
                    >
                      Health endpoint
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
          {health !== undefined && health.status === "DOWN" && (
            <div className="col-12">
              <div className="alert alert-danger mb-0 rounded-0">
                <div className="row align-items-center">
                  <div className="col">
                    <b>Deployment is unhealthy</b>
                  </div>
                  <div className="col-auto">
                    <button
                      className="btn btn-sm btn-outline-danger ms-3"
                      onClick={this.toggleHealthEndpoint}
                    >
                      Health endpoint
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
          {this.state.showHealthEndpoint && (
            <div className="mt-0">
              <ApiCall
                apiDocs="https://docs.datacater.io/docs/api/deployments/"
                apiPath={`/deployments/${deployment.uuid}/health`}
              />
            </div>
          )}
          {Object.keys(metrics).length > 0 && (
            <div className="col-12 mt-4">
              <div className="row align-items-center">
                {metricsOfInterest.map((metric) =>
                  this.renderMetric(metric, metrics)
                )}
                <div className="col-12 col-md-3 col-lg-2 mb-4 text-center">
                  <button
                    className="btn btn-sm btn-white"
                    onClick={this.toggleMetricsEndpoint}
                  >
                    Metrics endpoint
                  </button>
                </div>
              </div>
            </div>
          )}
          {this.state.showMetricsEndpoint && (
            <div className="mt-0 mb-4">
              <ApiCall
                apiDocs="https://docs.datacater.io/docs/api/deployments/"
                apiPath={`/deployments/${deployment.uuid}/metrics`}
              />
            </div>
          )}
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
  fetchDeploymentHealth: fetchDeploymentHealth,
  fetchDeploymentMetrics: fetchDeploymentMetrics,
};

export default connect(mapStateToProps, mapDispatchToProps)(ShowDeployment);

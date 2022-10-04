import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Creatable from "react-select/creatable";
import { Copy } from "react-feather";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { addStream } from "../../actions/streams";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import { getStreamConnectionOptions } from "../../helpers/getStreamConnectionOptions";
import { getStreamTopicOptions } from "../../helpers/getStreamTopicOptions";
import "../../scss/fonts.scss";

class NewStream extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingStreamFailed: false,
      errorMessages: {},
      showApiCall: false,
      stream: {
        spec: {
          kind: "KAFKA",
          kafka: {
            topic: {
              config: {},
            },
          },
        },
      },
      tempConfig: {
        topicName: "",
        topicValue: "",
        connectionName: "",
        connectionValue: "",
      },
      streamCreated: false,
    };

    this.handleCreateStream = this.handleCreateStream.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
    this.updateTempConfig = this.updateTempConfig.bind(this);
    this.addConfig = this.addConfig.bind(this);
    this.removeConfig = this.removeConfig.bind(this);
  }

  updateTempConfig(field, value) {
    let tempConfig = this.state.tempConfig;

    tempConfig[field] = value;

    this.setState({ tempConfig: tempConfig });
  }

  addConfig(event) {
    event.preventDefault();
    const tempConfig = this.state.tempConfig;
    let stream = this.state.stream;

    if (event.target.dataset.prefix === "spec.kafka") {
      stream.spec.kafka[tempConfig.connectionName] = tempConfig.connectionValue;
      this.setState({ stream: stream, tempConfig: tempConfig });
    } else if (event.target.dataset.prefix === "spec.kafka.topic") {
      if (
        ["num.partitions", "replication.factor"].includes(tempConfig.topicName)
      ) {
        stream.spec.kafka.topic[tempConfig.topicName] = tempConfig.topicValue;
      } else {
        stream.spec.kafka.topic.config[tempConfig.topicName] =
          tempConfig.topicValue;
      }
      this.setState({ stream: stream, tempConfig: tempConfig });
    }
  }

  removeConfig(event) {
    event.preventDefault();
    let stream = this.state.stream;
    const prefix = event.target.dataset.prefix;
    const config = event.target.dataset.config;

    if (prefix === "spec.kafka") {
      delete stream.spec.kafka[config];
      this.setState({ stream: stream });
    } else if (prefix === "spec.kafka.topic") {
      if (["num.partitions", "replication.factor"].includes(config)) {
        delete stream.spec.kafka.topic[config];
      } else {
        delete stream.spec.kafka.topic.config[config];
      }
      this.setState({ stream: stream });
    }
  }

  handleCreateStream(event) {
    event.preventDefault();

    this.props.addStream(this.state.stream).then(() => {
      if (this.props.streams.errorMessage !== undefined) {
        this.setState({
          streamCreated: false,
          errorMessage: this.props.streams.errorMessage,
        });
      } else {
        this.setState({
          streamCreated: true,
          errorMessage: "",
        });
      }
    });
  }

  handleChange(event) {
    let stream = this.state.stream;

    const newValue =
      event.target.type === "checkbox"
        ? event.target.checked
        : event.target.value;

    switch (event.target.dataset.prefix) {
      case "spec":
        stream["spec"][event.target.name] = newValue;
        break;
      case "spec.kafka":
        stream["spec"]["kafka"][event.target.name] = newValue;
        break;
      case "spec.kafka.topic":
        stream["spec"]["kafka"]["topic"][event.target.name] = newValue;
        break;
      default:
        stream[event.target.name] = newValue;
        break;
    }

    this.setState({
      creatingStreamFailed: false,
      errorMessage: "",
      stream: stream,
    });
  }

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
    });
  }

  render() {
    if (this.state.streamCreated) {
      return <Redirect to={"/streams/" + this.props.streams.stream.uuid} />;
    }

    const stream = this.state.stream;

    const topicOptions = getStreamTopicOptions();
    const connectionOptions = getStreamConnectionOptions();

    const addedTopicConfigs = Object.keys(stream.spec.kafka.topic)
      .filter((item) => item != "config")
      .concat(Object.keys(stream.spec.kafka.topic.config));

    const addedConnectionConfigs = Object.keys(stream.spec.kafka).filter(
      (item) => !["bootstrap.servers", "topic"].includes(item)
    );

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Streams", uri: "/streams" },
              { name: "New stream" },
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
                    <h4 className="fw-semibold mb-0">Create new stream</h4>
                    <p className="text-white mb-0">
                      Streams connect Apache Kafka® topics with your pipeline.
                    </p>
                  </div>
                  <div className="col-2 d-flex align-items-center justify-content-end">
                    <a
                      href="/streams/new"
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
                        href="https://docs.datacater.io/docs/api/streams/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light float-end"
                      >
                        See docs
                      </a>
                      <a
                        href="/streams/new/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light me-2 float-end"
                        onClick={(e) => {
                          e.preventDefault();
                          navigator.clipboard.writeText(
                            "curl " +
                              getApiPathPrefix(true) +
                              "/streams -XPOST -H'Content-Type:application/json' -H'Authorization:Bearer YOUR_TOKEN' -d'" +
                              JSON.stringify(this.state.stream) +
                              "'"
                          );
                        }}
                      >
                        <Copy className="feather-icon" />
                      </a>
                      <code className="text-white">
                        $ curl {getApiPathPrefix(true)}/streams/ \<br />
                        <span className="me-2"></span> -XPOST \<br />
                        <span className="me-2"></span>{" "}
                        -H&apos;Authorization:Bearer YOUR_TOKEN&apos; \<br />
                        <span className="me-2"></span>{" "}
                        -H&apos;Content-Type:application/json&apos; \<br />
                        <span className="me-2"></span> -d&apos;
                        {JSON.stringify(this.state.stream)}&apos;
                      </code>
                    </pre>
                  </div>
                )}
              </div>
            </div>
          </div>
          <form>
            <div className="col-12 mt-4"></div>
            <div className="col-12">
              <label htmlFor="name" className="form-label">
                <h5 className="fw-semibold">Name</h5>
                <span className="text-muted fs-7">
                  Name of the Apache Kafka topic. If the topic does not yet
                  exist on the broker, we will automatically create it.
                </span>
              </label>
              <input
                type="text"
                className="form-control"
                id="name"
                name="name"
                onChange={this.handleChange}
                value={this.state.stream["name"] || ""}
              />
            </div>
            <div className="col-12 mt-4 mb-0">
              <h5 className="fw-semibold">Topic configuration</h5>
              <span className="text-muted fs-7 mb-0">
                You can here use{" "}
                <a
                  href="https://kafka.apache.org/documentation/#topicconfigs"
                  target="_blank"
                >
                  topic-level options
                </a>
                .
              </span>
            </div>
            {addedTopicConfigs.length > 0 && (
              <div className="list-group mt-4">
                {addedTopicConfigs.map((topicConfig) => (
                  <div
                    className="list-group-item list-group-item-action bg-white p-3"
                    key={topicConfig}
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h6 className="d-flex align-items-center">
                        {topicConfig}
                      </h6>
                      <a
                        className="btn btn-sm btn-outline-primary d-flex align-items-center"
                        data-config={topicConfig}
                        data-prefix="spec.kafka.topic"
                        href="/streams/new"
                        onClick={this.removeConfig}
                      >
                        Remove
                      </a>
                    </div>
                    <small>
                      {stream.spec.kafka.topic[topicConfig] ||
                        stream.spec.kafka.topic.config[topicConfig]}
                    </small>
                  </div>
                ))}
              </div>
            )}
            <div className="col-12 mt-4">
              <h6>Add or update configuration</h6>
            </div>
            <div className="col-12">
              <div className="row">
                <div className="col-md-3">
                  <label className="form-label">Name</label>
                  <Creatable
                    isSearchable
                    options={topicOptions}
                    onChange={(value) => {
                      this.updateTempConfig("topicName", value.value);
                    }}
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Value</label>
                  <input
                    type="text"
                    className="form-control"
                    name="topicValue"
                    onChange={(event) => {
                      this.updateTempConfig("topicValue", event.target.value);
                    }}
                    value={this.state.tempConfig.topicValue || ""}
                  />
                </div>
                <div className="col-md-3 d-flex align-items-end">
                  <a
                    href="/streams/new"
                    className="btn btn-outline-primary"
                    data-prefix="spec.kafka.topic"
                    onClick={this.addConfig}
                  >
                    Add
                  </a>
                </div>
              </div>
            </div>
            <hr className="my-4" />
            <div className="col-12 mt-4">
              <h5 className="fw-semibold">Connection configuration</h5>
              <span className="text-muted fs-7">
                You can here use{" "}
                <a
                  href="https://kafka.apache.org/documentation/#consumerconfigs"
                  target="_blank"
                >
                  consumer-level
                </a>{" "}
                and{" "}
                <a
                  href="https://kafka.apache.org/documentation/#producerconfigs"
                  target="_blank"
                >
                  producer-level
                </a>{" "}
                options.
              </span>
            </div>
            <div className="col-12 mt-2">
              <label htmlFor="bootstrap.servers" className="form-label">
                bootstrap.servers
              </label>
              <input
                type="text"
                className="form-control"
                id="bootstrap.servers"
                data-prefix="spec.kafka"
                name="bootstrap.servers"
                onChange={this.handleChange}
                value={this.state.stream.spec.kafka["bootstrap.servers"] || ""}
              />
            </div>
            {addedConnectionConfigs.length > 0 && (
              <div className="list-group mt-4">
                {addedConnectionConfigs.map((connectionConfig) => (
                  <div
                    className="list-group-item list-group-item-action bg-white p-3"
                    key={connectionConfig}
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h6 className="d-flex align-items-center">
                        {connectionConfig}
                      </h6>
                      <a
                        className="btn btn-sm btn-outline-primary d-flex align-items-center"
                        data-config={connectionConfig}
                        data-prefix="spec.kafka"
                        href="/streams/new"
                        onClick={this.removeConfig}
                      >
                        Remove
                      </a>
                    </div>
                    <small>{stream.spec.kafka[connectionConfig]}</small>
                  </div>
                ))}
              </div>
            )}
            <div className="col-12 mt-3">
              <h6>Add or update configuration</h6>
            </div>
            <div className="col-12">
              <div className="row">
                <div className="col-md-3">
                  <label className="form-label">Name</label>
                  <Creatable
                    isSearchable
                    options={connectionOptions}
                    onChange={(value) => {
                      this.updateTempConfig("connectionName", value.value);
                    }}
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Value</label>
                  <input
                    type="text"
                    className="form-control"
                    name="topicValue"
                    onChange={(event) => {
                      this.updateTempConfig(
                        "connectionValue",
                        event.target.value
                      );
                    }}
                    value={this.state.tempConfig.connectionValue || ""}
                  />
                </div>
                <div className="col-md-3 d-flex align-items-end">
                  <a
                    href="/streams/new"
                    className="btn btn-outline-primary"
                    data-prefix="spec.kafka"
                    onClick={this.addConfig}
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
                href="/streams/new"
                className="btn btn-primary text-white mb-4"
                onClick={this.handleCreateStream}
              >
                Create stream
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
    streams: state.streams,
  };
};

const mapDispatchToProps = {
  addStream: addStream,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewStream);

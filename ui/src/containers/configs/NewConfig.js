import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Creatable from "react-select/creatable";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { getConfigKindOptions } from "../../helpers/getConfigKindOptions";
import Header from "../../components/layout/Header";
import Tabs from "../../components/configs/Tabs";
import { addConfig } from "../../actions/configs";
import { fetchPipelines } from "../../actions/pipelines";
import { getStreamConnectionOptions } from "../../helpers/getStreamConnectionOptions";
import { getStreamTopicOptions } from "../../helpers/getStreamTopicOptions";
import { getDeserializerOptions } from "../../helpers/getDeserializerOptions";
import { getSerializerOptions } from "../../helpers/getSerializerOptions";
import { isStreamHoldingAvroFormat } from "../../helpers/isStreamHoldingAvroFormat";
import "../../scss/fonts.scss";

class NewConfig extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingConfigFailed: false,
      currentTab: "config",
      showTopicConfig: false,
      config: {
        kind: "STREAM",
        metadata: {
          labels: {},
        },
        spec: {
          kind: "KAFKA",
          kafka: {
            topic: {
              config: {},
            },
          },
        },
      },
      tempLabel: {
        labelKey: "",
        labelValue: "",
      },
      deployment: {
        spec: {},
      },
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
      tempStreamConfig: {
        topicName: "",
        topicValue: "",
        connectionName: "",
        connectionValue: "",
      },
      configCreated: false,
    };

    this.handleCreateConfig = this.handleCreateConfig.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleEventChange = this.handleEventChange.bind(this);
    this.addLabel = this.addLabel.bind(this);
    this.removeLabel = this.removeLabel.bind(this);
    this.toggleShowTopicConfig = this.toggleShowTopicConfig.bind(this);
    this.addStreamConfig = this.addStreamConfig.bind(this);
    this.removeStreamConfig = this.removeStreamConfig.bind(this);
    this.updateConfigSpec = this.updateConfigSpec.bind(this);
    this.updateTab = this.updateTab.bind(this);
  }

  updateConfigSpec() {
    let config = this.state.config;
    let deployment = this.state.deployment;
    let stream = this.state.stream;
    const currentKind = config.kind;

    if (currentKind === "STREAM") {
      config.spec = stream.spec;
    } else {
      config.spec = deployment.spec;
    }

    this.setState({ config: config });
  }

  updateTempStreamConfig(field, value) {
    let tempStreamConfig = this.state.tempStreamConfig;

    tempStreamConfig[field] = value;
    this.setState({ tempStreamConfig: tempStreamConfig });
    this.updateConfigSpec();
  }

  updateConnectionConfig(field, value) {
    let stream = this.state.stream;

    stream.spec.kafka[field] = value;
    this.setState({ stream: stream });
    this.updateConfigSpec();
  }

  componentDidMount() {
    this.props.fetchPipelines();
  }

  updateTempLabel(field, value) {
    let tempLabel = this.state.tempLabel;
    tempLabel[field] = value;
    this.setState({ tempLabel: tempLabel });
  }

  addLabel(event) {
    event.preventDefault();
    const tempLabel = this.state.tempLabel;
    let config = this.state.config;
    config.metadata.labels[tempLabel.labelKey] = tempLabel.labelValue;
    this.setState({
      config: config,
      tempLabel: {
        labelKey: "",
        labelValue: "",
      },
    });
  }

  handleCreateConfig(event) {
    event.preventDefault();
    this.updateConfigSpec();
    this.props.addConfig(this.state.config).then(() => {
      if (this.props.configs.errorMessage !== undefined) {
        this.setState({
          configCreated: false,
          errorMessage: this.props.configs.errorMessage,
        });
      } else {
        this.setState({
          configCreated: true,
          errorMessage: "",
        });
      }
    });
  }

  handleEventChange(event) {
    let stream = this.state.stream;
    let config = this.state.config;

    const newValue =
      event.target.type === "checkbox"
        ? event.target.checked
        : event.target.value;

    switch (event.target.dataset.prefix) {
      case "stream.spec":
        stream["spec"][event.target.name] = newValue;
        break;
      case "stream.spec.kafka":
        stream["spec"]["kafka"][event.target.name] = newValue;
        break;
      case "stream.spec.kafka.topic":
        stream["spec"]["kafka"]["topic"][event.target.name] = newValue;
        break;
      case "stream.spec.kafka.topic.config":
        stream["spec"]["kafka"]["topic"]["config"][event.target.name] =
          newValue;
        break;
      case "metadata.labels":
        config.metadata.labels[event.target.name] = newValue;
        break;
      default:
        stream[event.target.name] = newValue;
        break;
    }
    this.setState({
      creatingConfigFailed: false,
      errorMessage: undefined,
      stream: stream,
      config: config,
    });
    this.updateConfigSpec();
  }

  handleChange(name, value, prefix) {
    let config = this.state.config;
    let deployment = this.state.deployment;
    let stream = this.state.stream;

    if (prefix === undefined) {
      prefix = "";
    }

    if (prefix === "deployment.spec") {
      deployment.spec[name] = value;
    } else {
      config[name] = value;
    }

    this.setState({
      creatingConfigFailed: false,
      errorMessage: undefined,
      config: config,
      deployment: deployment,
      stream: stream,
    });
    this.updateConfigSpec();
  }

  removeLabel(event) {
    event.preventDefault();
    let config = this.state.config;
    const label = event.target.dataset.label;
    delete config.metadata.labels[label];
    this.setState({ config: config });
  }

  updateKindOption(field, value) {
    let config = this.state.config;

    config.kind = value;

    this.setState({
      config: config,
      errorMessage: undefined,
    });
    this.updateConfigSpec();
  }

  setDefaultKind(field, value) {
    let config = this.state.config;

    config.kind = value;

    this.setState({ config: config });
    return this.state.config.kind;
  }

  toggleShowTopicConfig(event) {
    event.preventDefault();

    this.setState({
      showTopicConfig: !this.state.showTopicConfig,
    });
  }

  addStreamConfig(event) {
    event.preventDefault();
    const tempStreamConfig = this.state.tempStreamConfig;
    let stream = this.state.stream;

    if (event.target.dataset.prefix === "stream.spec.kafka") {
      stream.spec.kafka[tempStreamConfig.connectionName] =
        tempStreamConfig.connectionValue;
      this.setState({ stream: stream, tempStreamConfig: tempStreamConfig });
    } else if (
      event.target.dataset.prefix === "stream.spec.kafka.topic.config"
    ) {
      stream.spec.kafka.topic.config[tempStreamConfig.topicName] =
        tempStreamConfig.topicValue;
      this.setState({ stream: stream, tempStreamConfig: tempStreamConfig });
    }
    this.updateConfigSpec();
  }

  removeStreamConfig(event) {
    event.preventDefault();
    let stream = this.state.stream;
    const prefix = event.target.dataset.prefix;
    const streamConfig = event.target.dataset.config;

    if (prefix === "stream.spec.kafka") {
      delete stream.spec.kafka[streamConfig];
    } else if (prefix === "stream.spec.kafka.topic.config") {
      delete stream.spec.kafka.topic.config[streamConfig];
    }

    this.setState({ stream: stream });
    this.updateConfigSpec();
  }

  updateConnectionConfig(field, value) {
    let stream = this.state.stream;
    stream.spec.kafka[field] = value;

    this.setState({ stream: stream });
    this.updateConfigSpec();
  }

  updateTab(event) {
    event.preventDefault();
    this.setState({
      currentTab: event.target.dataset.tab,
    });
  }

  render() {
    if (this.state.configCreated) {
      return <Redirect to={"/configs/" + this.props.configs.config.uuid} />;
    }

    const kindOptions = getConfigKindOptions();
    const config = this.state.config;
    const addedLabels = Object.keys(config.metadata.labels);

    const pipelineOptions = this.props.pipelines.pipelines.map((pipeline) => {
      const name = `${pipeline.name || "Untitled pipeline"} (${pipeline.uuid})`;
      return { value: pipeline.uuid, label: name };
    });

    const stream = this.state.stream;

    const topicOptions = getStreamTopicOptions();
    const connectionOptions = getStreamConnectionOptions();
    const deserializerOptions = getDeserializerOptions(stream);
    const serializerOptions = getSerializerOptions(stream);

    const defaultDeserializer = "io.datacater.core.serde.JsonDeserializer";
    const defaultSerializer = "io.datacater.core.serde.JsonSerializer";

    const addedTopicConfigs = Object.keys(stream.spec.kafka.topic.config);
    const addedConnectionConfigs = Object.keys(stream.spec.kafka).filter(
      (item) =>
        ![
          "bootstrap.servers",
          "key.deserializer",
          "value.deserializer",
          "key.serializer",
          "value.serializer",
          "schema.registry.url",
          "topic",
        ].includes(item)
    );

    const streamHoldsAvroFormat = isStreamHoldingAvroFormat(stream);

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Configs", uri: "/configs" },
              { name: "New config" },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/configs/"
            apiPath="/configs/"
            httpMethod="POST"
            requestBody={this.state.config}
            title="Create new config"
            subTitle="Configs outsource the configuration of Streams and other resources."
          />
          <form>
            <div className="row">
              <div className="col-4 col-md-3 col-lg-2">
                <Tabs
                  currentTab={this.state.currentTab}
                  updateTabFunc={this.updateTab}
                />
              </div>
              <div className="col-8 col-md-9 col-lg-10">
                {this.state.currentTab === "config" && (
                  <>
                    <div className="col-12 mt-4">
                      <label
                        htmlFor="name"
                        className="form-label h5 fw-semibold mb-3"
                      >
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
                        value={this.state.config["name"] || ""}
                      />
                    </div>
                    <div className="col-12 mt-4">
                      <h5 className="d-inline me-2 fw-semibold">Labels</h5>
                      <span className="text-muted fs-7">
                        Configs can be referenced by other resources, e.g.,
                        Streams, via their labels.
                      </span>
                    </div>
                    {addedLabels.length === 0 && (
                      <div className="col-12 mt-2 mb-n1">
                        <i>No labels defined.</i>
                      </div>
                    )}
                    {addedLabels.length > 0 &&
                      addedLabels.map((label) => (
                        <div className="col-12 mt-2" key={label}>
                          <label htmlFor={label} className="form-label">
                            Key: {label}
                            <a
                              className="ms-2 fs-7"
                              data-label={label}
                              data-prefix="metadata.labels"
                              href="/configs/new"
                              onClick={this.removeLabel}
                            >
                              Remove label
                            </a>
                          </label>
                          <input
                            type="text"
                            className="form-control"
                            id={label}
                            data-prefix="metadata.labels"
                            name={label}
                            onChange={this.handleEventChange}
                            value={
                              this.state.config.metadata.labels[label] || ""
                            }
                          />
                        </div>
                      ))}
                    <div class="col-12 mt-3">
                      <h6 class="d-inline me-2">Add label</h6>
                    </div>
                    <div className="col-12 mt-1">
                      <div className="row">
                        <div className="col-md-3">
                          <label className="form-label">Key</label>
                          <input
                            type="text"
                            className="form-control"
                            name="labelKey"
                            onChange={(event) => {
                              this.updateTempLabel(
                                "labelKey",
                                event.target.value
                              );
                            }}
                            value={this.state.tempLabel.labelKey || ""}
                          />
                        </div>
                        <div className="col-md-3">
                          <label className="form-label">Value</label>
                          <input
                            type="text"
                            className="form-control"
                            name="labelValue"
                            onChange={(event) => {
                              this.updateTempLabel(
                                "labelValue",
                                event.target.value
                              );
                            }}
                            value={this.state.tempLabel.labelValue || ""}
                          />
                        </div>
                        <div className="col-md-3 d-flex align-items-end">
                          <a
                            href="/configs/new"
                            className="btn btn-outline-primary"
                            data-prefix="metadata.labels"
                            onClick={this.addLabel}
                          >
                            Add
                          </a>
                        </div>
                      </div>
                    </div>
                    <div className="col-12 mt-4">
                      <h5 className="fw-semibold mb-3">Kind</h5>
                      <Creatable
                        defaultValue={kindOptions.find(
                          (kind) => kind.value === config.kind
                        )}
                        isSearchable
                        options={kindOptions}
                        onChange={(kind) => {
                          this.updateKindOption("kind", kind.value);
                        }}
                      />
                    </div>
                  </>
                )}
                {this.state.currentTab === "spec" && (
                  <>
                    {this.state.config["kind"] == "STREAM" && (
                      <>
                        <div className="col-12 mt-4">
                          <h5 className="fw-semibold">Topic configuration</h5>
                        </div>
                        <div className="col-12 mt-2">
                          <label
                            htmlFor="num.partitions"
                            className="form-label"
                          >
                            num.partitions
                          </label>
                          <input
                            type="text"
                            className="form-control"
                            id="num.partitions"
                            data-prefix="stream.spec.kafka.topic"
                            name="num.partitions"
                            onChange={this.handleEventChange}
                            placeholder="3"
                            value={
                              this.state.stream.spec.kafka["topic"][
                                "num.partitions"
                              ] || ""
                            }
                          />
                        </div>
                        <div className="col-12 mt-2">
                          <label
                            htmlFor="replication.factor"
                            className="form-label"
                          >
                            replication.factor
                          </label>
                          <input
                            type="text"
                            className="form-control"
                            id="replication.factor"
                            data-prefix="stream.spec.kafka.topic"
                            name="replication.factor"
                            onChange={this.handleEventChange}
                            placeholder="1"
                            value={
                              this.state.stream.spec.kafka["topic"][
                                "replication.factor"
                              ] || ""
                            }
                          />
                        </div>
                        {addedTopicConfigs.map((topicConfig) => (
                          <div className="col-12 mt-2" key={topicConfig}>
                            <label htmlFor={topicConfig} className="form-label">
                              {topicConfig}
                              <a
                                className="ms-2 fs-7"
                                data-config={topicConfig}
                                data-prefix="stream.spec.kafka.topic.config"
                                href="/configs/new"
                                onClick={this.removeStreamConfig}
                              >
                                Remove
                              </a>
                            </label>
                            <input
                              type="text"
                              className="form-control"
                              id={topicConfig}
                              data-prefix="stream.spec.kafka.topic.config"
                              name={topicConfig}
                              onChange={this.handleEventChange}
                              value={
                                this.state.stream.spec.kafka.topic.config[
                                  topicConfig
                                ] || ""
                              }
                            />
                          </div>
                        ))}
                        <div className="col-12 mt-3">
                          <h6 className="d-inline me-2">Add config</h6>
                          <span className="text-muted fs-7">
                            You can here use{" "}
                            <a
                              href="https://kafka.apache.org/documentation/#topicconfigs"
                              target="_blank"
                            >
                              topic-level
                            </a>{" "}
                            configuration options.
                          </span>
                        </div>
                        <div className="col-12 mt-2">
                          <div className="row">
                            <div className="col-md-3">
                              <label className="form-label">Name</label>
                              <Creatable
                                isSearchable
                                options={topicOptions}
                                onChange={(value) => {
                                  this.updateTempStreamConfig(
                                    "topicName",
                                    value.value
                                  );
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
                                  this.updateTempStreamConfig(
                                    "topicValue",
                                    event.target.value
                                  );
                                }}
                                value={
                                  this.state.tempStreamConfig.topicValue || ""
                                }
                              />
                            </div>
                            <div className="col-md-3 d-flex align-items-end">
                              <a
                                href="/configs/new"
                                className="btn btn-outline-primary"
                                data-prefix="stream.spec.kafka.topic.config"
                                onClick={this.addStreamConfig}
                              >
                                Add
                              </a>
                            </div>
                          </div>
                        </div>
                        <div className="col-12 mt-4">
                          <h5 className="fw-semibold">Data format</h5>
                        </div>
                        <div className="col-12">
                          <label
                            htmlFor="key.deserializer"
                            className="form-label"
                          >
                            key.deserializer
                          </label>
                          <Creatable
                            defaultValue={deserializerOptions.find(
                              (deserializer) =>
                                deserializer.value === defaultDeserializer
                            )}
                            isSearchable
                            options={deserializerOptions}
                            onChange={(value) => {
                              this.updateConnectionConfig(
                                "key.deserializer",
                                value.value
                              );
                            }}
                          />
                        </div>
                        <div className="col-12 mt-2">
                          <label
                            htmlFor="value.deserializer"
                            className="form-label"
                          >
                            value.deserializer
                          </label>
                          <Creatable
                            defaultValue={deserializerOptions.find(
                              (deserializer) =>
                                deserializer.value === defaultDeserializer
                            )}
                            isSearchable
                            options={deserializerOptions}
                            onChange={(value) => {
                              this.updateConnectionConfig(
                                "value.deserializer",
                                value.value
                              );
                            }}
                          />
                        </div>
                        <div className="col-12 mt-2">
                          <label
                            htmlFor="key.serializer"
                            className="form-label"
                          >
                            key.serializer
                          </label>
                          <Creatable
                            defaultValue={serializerOptions.find(
                              (deserializer) =>
                                deserializer.value === defaultSerializer
                            )}
                            isSearchable
                            options={serializerOptions}
                            onChange={(value) => {
                              this.updateConnectionConfig(
                                "key.serializer",
                                value.value
                              );
                            }}
                          />
                        </div>
                        <div className="col-12 mt-2">
                          <label
                            htmlFor="value.serializer"
                            className="form-label"
                          >
                            value.serializer
                          </label>
                          <Creatable
                            defaultValue={serializerOptions.find(
                              (deserializer) =>
                                deserializer.value === defaultSerializer
                            )}
                            isSearchable
                            options={serializerOptions}
                            onChange={(value) => {
                              this.updateConnectionConfig(
                                "value.serializer",
                                value.value
                              );
                            }}
                          />
                        </div>
                        {streamHoldsAvroFormat && (
                          <>
                            <div className="col-12 mt-2">
                              <label
                                htmlFor="schema.registry.url"
                                className="form-label"
                              >
                                schema.registry.url
                              </label>
                              <input
                                type="text"
                                className="form-control"
                                id="schema.registry.url"
                                data-prefix="stream.spec.kafka"
                                name="schema.registry.url"
                                onChange={this.handleEventChange}
                                value={
                                  this.state.stream.spec.kafka[
                                    "schema.registry.url"
                                  ] || ""
                                }
                              />
                            </div>
                          </>
                        )}
                        <div className="col-12 mt-4">
                          <h5 className="fw-semibold">Connection</h5>
                        </div>
                        <div className="col-12 mt-2">
                          <label
                            htmlFor="bootstrap.servers"
                            className="form-label"
                          >
                            bootstrap.servers
                          </label>
                          <input
                            type="text"
                            className="form-control"
                            id="bootstrap.servers"
                            data-prefix="stream.spec.kafka"
                            name="bootstrap.servers"
                            onChange={this.handleEventChange}
                            value={
                              this.state.stream.spec.kafka[
                                "bootstrap.servers"
                              ] || ""
                            }
                          />
                        </div>
                        {addedConnectionConfigs.map((connectionConfig) => (
                          <div className="col-12 mt-2" key={connectionConfig}>
                            <label
                              htmlFor={connectionConfig}
                              className="form-label"
                            >
                              {connectionConfig}
                              <a
                                className="ms-2 fs-7"
                                data-config={connectionConfig}
                                data-prefix="stream.spec.kafka"
                                href="/configs/new"
                                onClick={this.removeStreamConfig}
                              >
                                Remove
                              </a>
                            </label>
                            <input
                              type="text"
                              className="form-control"
                              id={connectionConfig}
                              data-prefix="stream.spec.kafka"
                              name={connectionConfig}
                              onChange={this.handleEventChange}
                              value={
                                this.state.stream.spec.kafka[
                                  connectionConfig
                                ] || ""
                              }
                            />
                          </div>
                        ))}
                        <div className="col-12 mt-3">
                          <h6 className="d-inline me-2">Add config</h6>
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
                            configuration options.
                          </span>
                        </div>
                        <div className="col-12 mt-2">
                          <div className="row">
                            <div className="col-md-3">
                              <label className="form-label">Name</label>
                              <Creatable
                                isSearchable
                                options={connectionOptions}
                                onChange={(value) => {
                                  this.updateTempStreamConfig(
                                    "connectionName",
                                    value.value
                                  );
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
                                  this.updateTempStreamConfig(
                                    "connectionValue",
                                    event.target.value
                                  );
                                }}
                                value={
                                  this.state.tempStreamConfig.connectionValue ||
                                  ""
                                }
                              />
                            </div>
                            <div className="col-md-3 d-flex align-items-end">
                              <a
                                href="/configs/new"
                                className="btn btn-outline-primary"
                                data-prefix="stream.spec.kafka"
                                onClick={this.addStreamConfig}
                              >
                                Add
                              </a>
                            </div>
                          </div>
                        </div>
                      </>
                    )}
                    {this.state.config["kind"] == "DEPLOYMENT" && (
                      <>
                        <div className="col-12 mt-4">
                          <h5 className="fw-semibold">Deployment config</h5>
                        </div>
                        <div className="col-12">
                          <label className="form-label">Pipeline</label>
                          <Select
                            defaultValue={pipelineOptions.find(
                              (kind) => kind.value === config.spec.pipeline
                            )}
                            isSearchable
                            isClearable
                            options={pipelineOptions}
                            onChange={(value) => {
                              this.handleChange(
                                "pipeline",
                                value.value,
                                "deployment.spec"
                              );
                            }}
                          />
                        </div>
                      </>
                    )}
                  </>
                )}
                {![undefined, ""].includes(this.state.errorMessage) && (
                  <div className="alert alert-danger mt-4">
                    <p className="h6 fs-bolder">API response:</p>
                    {this.state.errorMessage}
                  </div>
                )}
                <div className="col-12 mt-4">
                  <a
                    href="/configs/new"
                    className="btn btn-primary text-white mb-4"
                    onClick={this.handleCreateConfig}
                  >
                    Create config
                  </a>
                </div>
              </div>
            </div>
          </form>
        </div>
      </div>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    configs: state.configs,
    pipelines: state.pipelines,
  };
};

const mapDispatchToProps = {
  addConfig: addConfig,
  fetchPipelines: fetchPipelines,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewConfig);

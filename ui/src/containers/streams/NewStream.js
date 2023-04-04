import React, { Component } from "react";
import { connect } from "react-redux";
import { PayloadEditor } from "../../components/payload-editor/PayloadEditor";
import { Redirect } from "react-router-dom";
import Creatable from "react-select/creatable";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { addStream } from "../../actions/streams";
import { getStreamConnectionOptions } from "../../helpers/getStreamConnectionOptions";
import { getStreamTopicOptions } from "../../helpers/getStreamTopicOptions";
import { getDeserializerOptions } from "../../helpers/getDeserializerOptions";
import { getSerializerOptions } from "../../helpers/getSerializerOptions";
import { isStreamHoldingAvroFormat } from "../../helpers/isStreamHoldingAvroFormat";
import "../../scss/fonts.scss";

class NewStream extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingStreamFailed: false,
      payloadEditorChanges: false, // indicate, if the payload editor introduced changes
      payloadEditorCode: "", // payloadEditorCode as string, only valid during editing
      showPayloadEditor: false,
      errorMessages: {},
      showTopicConfig: false,
      editorStream: {
        spec: {
          kind: "KAFKA",
          kafka: {
            topic: {
              config: {},
            },
          },
        },
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
    this.toggleShowTopicConfig = this.toggleShowTopicConfig.bind(this);
    this.updateTempConfig = this.updateTempConfig.bind(this);
    this.updateConnectionConfig = this.updateConnectionConfig.bind(this);
    this.addConfig = this.addConfig.bind(this);
    this.removeConfig = this.removeConfig.bind(this);
    this.loadHTMLForm = this.loadHTMLForm.bind(this);
    this.loadPayloadEditor = this.loadPayloadEditor.bind(this);
    this.toggleForm = this.toggleForm.bind(this);
    this.handleEditorChange = this.handleEditorChange.bind(this);
    this.submitForm = this.submitForm.bind(this);
    this.submitEditorContent = this.submitEditorContent.bind(this);
  }

  updateTempConfig(field, value) {
    let tempConfig = this.state.tempConfig;

    tempConfig[field] = value;

    this.setState({ tempConfig: tempConfig });
  }

  updateConnectionConfig(field, value) {
    let stream = this.state.stream;

    stream.spec.kafka[field] = value;

    this.setState({ stream: stream });
  }

  addConfig(event) {
    event.preventDefault();
    const tempConfig = this.state.tempConfig;
    let stream = this.state.stream;

    if (event.target.dataset.prefix === "spec.kafka") {
      stream.spec.kafka[tempConfig.connectionName] = tempConfig.connectionValue;
      this.setState({ stream: stream, tempConfig: tempConfig });
    } else if (event.target.dataset.prefix === "spec.kafka.topic.config") {
      stream.spec.kafka.topic.config[tempConfig.topicName] =
        tempConfig.topicValue;
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
    } else if (prefix === "spec.kafka.topic.config") {
      delete stream.spec.kafka.topic.config[config];
    }

    this.setState({ stream: stream });
  }

  loadHTMLForm(stream) {
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
      <form>
        <div className="col-12 mt-4">
          <label htmlFor="name" className="form-label">
            <h5 className="fw-semibold">
              Name<span className="text-danger ms-1">*</span>
            </h5>
            <span className="text-muted fs-7 me-2">
              Name of the Apache Kafka topic. If the topic does not yet exist on
              the broker, we will automatically create it.
            </span>
            <a
              className="fs-7"
              href="/streams/new"
              onClick={this.toggleShowTopicConfig}
            >
              {!this.state.showTopicConfig && "Edit topic config"}
              {this.state.showTopicConfig && "Hide topic config"}
            </a>
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
        {this.state.showTopicConfig && (
          <>
            <div className="card mt-4">
              <div className="card-body">
                <div className="col-12 d-flex align-items-center">
                  <h5 className="fw-semibold d-inline mb-0">
                    Topic configuration
                  </h5>
                  <a
                    className="fs-7 ms-2"
                    href="/streams/new"
                    onClick={this.toggleShowTopicConfig}
                  >
                    Hide
                  </a>
                </div>
                <div className="col-12 mt-2">
                  <label htmlFor="num.partitions" className="form-label">
                    num.partitions
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="num.partitions"
                    data-prefix="spec.kafka.topic"
                    name="num.partitions"
                    onChange={this.handleChange}
                    placeholder="1"
                    value={
                      this.state.stream.spec.kafka["topic"]["num.partitions"] ||
                      ""
                    }
                  />
                </div>
                <div className="col-12 mt-2">
                  <label htmlFor="replication.factor" className="form-label">
                    replication.factor
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="replication.factor"
                    data-prefix="spec.kafka.topic"
                    name="replication.factor"
                    onChange={this.handleChange}
                    placeholder="3"
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
                        data-prefix="spec.kafka.topic.config"
                        href="/streams/new"
                        onClick={this.removeConfig}
                      >
                        Remove
                      </a>
                    </label>
                    <input
                      type="text"
                      className="form-control"
                      id={topicConfig}
                      data-prefix="spec.kafka.topic.config"
                      name={topicConfig}
                      onChange={this.handleChange}
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
                          this.updateTempConfig(
                            "topicValue",
                            event.target.value
                          );
                        }}
                        value={this.state.tempConfig.topicValue || ""}
                      />
                    </div>
                    <div className="col-md-3 d-flex align-items-end">
                      <a
                        href="/streams/new"
                        className="btn btn-outline-primary"
                        data-prefix="spec.kafka.topic.config"
                        onClick={this.addConfig}
                      >
                        Add
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </>
        )}
        <div className="col-12 mt-4">
          <h5 className="fw-semibold">Data format</h5>
        </div>
        <div className="col-12">
          <label htmlFor="key.deserializer" className="form-label">
            key.deserializer
          </label>
          <Creatable
            defaultValue={deserializerOptions.find(
              (deserializer) => deserializer.value === defaultDeserializer
            )}
            isSearchable
            options={deserializerOptions}
            onChange={(value) => {
              this.updateConnectionConfig("key.deserializer", value.value);
            }}
          />
        </div>
        <div className="col-12 mt-2">
          <label htmlFor="value.deserializer" className="form-label">
            value.deserializer
          </label>
          <Creatable
            defaultValue={deserializerOptions.find(
              (deserializer) => deserializer.value === defaultDeserializer
            )}
            isSearchable
            options={deserializerOptions}
            onChange={(value) => {
              this.updateConnectionConfig("value.deserializer", value.value);
            }}
          />
        </div>
        <div className="col-12 mt-2">
          <label htmlFor="key.serializer" className="form-label">
            key.serializer
          </label>
          <Creatable
            defaultValue={serializerOptions.find(
              (deserializer) => deserializer.value === defaultSerializer
            )}
            isSearchable
            options={serializerOptions}
            onChange={(value) => {
              this.updateConnectionConfig("key.serializer", value.value);
            }}
          />
        </div>
        <div className="col-12 mt-2">
          <label htmlFor="value.serializer" className="form-label">
            value.serializer
          </label>
          <Creatable
            defaultValue={serializerOptions.find(
              (deserializer) => deserializer.value === defaultSerializer
            )}
            isSearchable
            options={serializerOptions}
            onChange={(value) => {
              this.updateConnectionConfig("value.serializer", value.value);
            }}
          />
        </div>
        {streamHoldsAvroFormat && (
          <>
            <div className="col-12 mt-2">
              <label htmlFor="schema.registry.url" className="form-label">
                schema.registry.url
              </label>
              <input
                type="text"
                className="form-control"
                id="schema.registry.url"
                data-prefix="spec.kafka"
                name="schema.registry.url"
                onChange={this.handleChange}
                value={
                  this.state.stream.spec.kafka["schema.registry.url"] || ""
                }
              />
            </div>
          </>
        )}
        <div className="col-12 mt-4">
          <h5 className="fw-semibold">Connection</h5>
        </div>
        <div className="col-12 mt-2">
          <label htmlFor="bootstrap.servers" className="form-label">
            bootstrap.servers<span className="text-danger ms-1">*</span>
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
        {addedConnectionConfigs.map((connectionConfig) => (
          <div className="col-12 mt-2" key={connectionConfig}>
            <label htmlFor={connectionConfig} className="form-label">
              {connectionConfig}
              <a
                className="ms-2 fs-7"
                data-config={connectionConfig}
                data-prefix="spec.kafka"
                href="/streams/new"
                onClick={this.removeConfig}
              >
                Remove
              </a>
            </label>
            <input
              type="text"
              className="form-control"
              id={connectionConfig}
              data-prefix="spec.kafka"
              name={connectionConfig}
              onChange={this.handleChange}
              value={this.state.stream.spec.kafka[connectionConfig] || ""}
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
                  this.updateTempConfig("connectionValue", event.target.value);
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
      </form>
    );
  }

  toggleForm(event) {
    event.preventDefault();
    let toggle = !this.state.showPayloadEditor;

    if (toggle) {
      this.setState({
        showPayloadEditor: toggle,
        editorStream: JSON.stringify(this.state.stream, null, 2),
      });
    } else if (this.state.payloadEditorChanges && !window.confirm("Going back will reset all edits in the editor!")) {
        this.setState({
          showPayloadEditor: true,
      });
    } else {
      this.setState({
        editorStream: JSON.stringify(this.state.stream, null, 2),
        showPayloadEditor: toggle,
        errorMessage: "",
        payloadEditorChanges: false,
      });
    }
  }

  handleEditorChange(value) {
    this.setState({
      editorStream: value,
      payloadEditorChanges: true,
    });
  }

  loadPayloadEditor(stream) {
    return (
      <div className="col-12 mt-4">
        <PayloadEditor
          apiPath="/streams/"
          code={this.state.editorStream}
          codeChange={this.handleEditorChange}
        ></PayloadEditor>
      </div>
    );
  }

  handleCreateStream(event) {
    event.preventDefault();
    if (this.state.showPayloadEditor) {
      this.submitEditorContent();
    } else {
      this.submitForm();
    }
  }

  submitEditorContent() {
    try {
      let parsedEditorStream = JSON.parse(this.state.editorStream);
      this.props.addStream(parsedEditorStream).then(() => {
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
    } catch (syntaxError) {
      this.setState({
        streamCreated: false,
        errorMessage: syntaxError.message,
      });
    }
  }

  submitForm() {
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
      case "spec.kafka.topic.config":
        stream["spec"]["kafka"]["topic"]["config"][event.target.name] =
          newValue;
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

  toggleShowTopicConfig(event) {
    event.preventDefault();

    this.setState({
      showTopicConfig: !this.state.showTopicConfig,
    });
  }

  render() {
    if (this.state.streamCreated) {
      return <Redirect to={"/streams/" + this.props.streams.stream.uuid} />;
    }

    const stream = this.state.stream;

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Streams", uri: "/streams" },
              { name: "New stream" },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/streams/"
            apiPath="/streams/"
            httpMethod="POST"
            requestBody={this.state.stream}
            title="Create new stream"
            subTitle="Streams connect Apache Kafka® topics with your pipeline."
          />
          {this.state.showPayloadEditor
            ? this.loadPayloadEditor(stream)
            : this.loadHTMLForm(stream)}
          {![undefined, ""].includes(this.state.errorMessage) && (
            <div className="alert alert-danger mt-4">
              <p className="h6 fs-bolder">API response:</p>
              {this.state.errorMessage}
            </div>
          )}
          <div className="col-12 my-4">
            <a
              href="/streams/new"
              className="btn btn-primary text-white"
              onClick={this.handleCreateStream}
            >
              Create stream
            </a>
            <button
              className="btn btn-outline-primary ms-2"
              onClick={this.toggleForm}
            >
              {this.state.showPayloadEditor ? "Back to form" : "Edit as JSON"}
            </button>
          </div>
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

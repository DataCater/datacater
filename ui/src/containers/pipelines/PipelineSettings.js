import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { fetchPipeline, updatePipeline } from "../../actions/pipelines";
import { fetchStreams } from "../../actions/streams";
import "../../scss/fonts.scss";

class PipelineSettings extends Component {
  constructor(props) {
    super(props);

    this.state = {
      updatingPipelineFailed: false,
      errorMessages: {},
      pipeline: undefined,
      pipelineUpdated: false,
    };

    this.handleUpdatePipeline = this.handleUpdatePipeline.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  componentDidMount() {
    this.props
      .fetchPipeline(this.getPipelineId())
      .then(() => this.setState({ pipeline: this.props.pipelines.pipeline }));
    this.props.fetchStreams();
  }

  getPipelineId() {
    return this.props.match.params.id;
  }

  handleUpdatePipeline(event) {
    event.preventDefault();

    this.props
      .updatePipeline(this.getPipelineId(), this.state.pipeline)
      .then(() => {
        if (this.props.pipelines.errorMessage !== undefined) {
          this.setState({
            pipelineUpdated: false,
            errorMessage: this.props.pipelines.errorMessage,
          });
        } else {
          this.setState({
            pipelineUpdated: true,
            errorMessage: "",
          });
        }
      });
  }

  handleChange(name, value, prefix) {
    let pipeline = this.state.pipeline;

    if (prefix === "metadata") {
      pipeline.metadata[name] = value;
    } else {
      pipeline[name] = value;
    }

    this.setState({
      creatingPipelineFailed: false,
      errorMessage: "",
      pipeline: pipeline,
    });
  }

  render() {
    if (this.state.pipelineUpdated) {
      return <Redirect to={`/pipelines/${this.getPipelineId()}`} />;
    }

    const pipeline = this.state.pipeline;

    if (pipeline === undefined) {
      return <></>;
    }

    const apiPayload = Object.assign({}, pipeline);
    delete apiPayload.uuid;
    delete apiPayload.createdAt;
    delete apiPayload.updatedAt;

    const streamOptions = this.props.streams.streams.map((stream) => {
      const name = `${stream.name || "Untitled stream"} (${stream.uuid})`;
      return { value: stream.uuid, label: name };
    });

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Pipelines", uri: "/pipelines" },
              { name: pipeline.uuid, uri: `/pipelines/${pipeline.uuid}` },
              { name: "Settings" },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/pipelines/"
            apiPath={`/pipelines/${pipeline.uuid}`}
            httpMethod="PUT"
            requestBody={apiPayload}
            title={pipeline.name || "Untitled pipeline"}
          />
          <form>
            <div className="col-12 mt-4">
              <label htmlFor="name" className="form-label">
                Name{" "}
              </label>
              <input
                type="text"
                className="form-control"
                id="name"
                name="name"
                onChange={(event) => {
                  this.handleChange("name", event.target.value);
                }}
                value={this.state.pipeline["name"] || ""}
              />
            </div>
            <div className="col-12 mt-4">
              <label className="form-label">Source stream</label>
              <Select
                isSearchable
                isClearable
                defaultValue={
                  streamOptions.filter(
                    (option) => option.value === pipeline.metadata["stream-in"]
                  )[0]
                }
                options={streamOptions}
                onChange={(value) => {
                  this.handleChange("stream-in", value.value, "metadata");
                }}
              />
            </div>
            <div className="col-12 mt-4">
              <label className="form-label">Sink stream</label>
              <Select
                isSearchable
                isClearable
                defaultValue={
                  streamOptions.filter(
                    (option) => option.value === pipeline.metadata["stream-out"]
                  )[0]
                }
                options={streamOptions}
                onChange={(value) => {
                  this.handleChange("stream-out", value.value, "metadata");
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
                href={`/pipelines/${pipeline.uuid}`}
                className="btn btn-primary text-white mb-4"
                onClick={this.handleUpdatePipeline}
              >
                Update pipeline
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
    pipelines: state.pipelines,
    streams: state.streams,
  };
};

const mapDispatchToProps = {
  fetchPipeline: fetchPipeline,
  updatePipeline: updatePipeline,
  fetchStreams: fetchStreams,
};

export default connect(mapStateToProps, mapDispatchToProps)(PipelineSettings);

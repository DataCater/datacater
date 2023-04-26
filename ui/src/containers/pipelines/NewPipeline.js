import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { addPipeline } from "../../actions/pipelines";
import { fetchStreams } from "../../actions/streams";
import "../../scss/fonts.scss";

class NewPipeline extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingPipelineFailed: false,
      errorMessages: {},
      pipeline: {
        metadata: {},
        spec: {},
      },
      pipelineCreated: false,
    };

    this.handleCreatePipeline = this.handleCreatePipeline.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  componentDidMount() {
    this.props.fetchStreams();
  }

  handleCreatePipeline(event) {
    event.preventDefault();

    this.props.addPipeline(this.state.pipeline).then(() => {
      if (this.props.pipelines.errorMessage !== undefined) {
        this.setState({
          pipelineCreated: false,
          errorMessage: this.props.pipelines.errorMessage,
        });
      } else {
        this.setState({
          pipelineCreated: true,
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
    if (this.state.pipelineCreated) {
      return (
        <Redirect to={"/pipelines/" + this.props.pipelines.pipeline.uuid} />
      );
    }

    const pipeline = this.state.pipeline;

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
              { name: "New pipeline" },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/pipelines/"
            apiPath="/pipelines/"
            httpMethod="POST"
            requestBody={this.state.pipeline}
            title="Create new pipeline"
            subTitle="Pipelines stream records between your Streams and can apply filters and transforms on the way."
          />
          <form>
            <div className="col-12 mt-4">
              <label htmlFor="name" className="form-label">
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
                value={this.state.pipeline["name"] || ""}
              />
            </div>
            <div className="col-12 mt-4">
              <label className="form-label">Source stream</label>
              <Select
                isSearchable
                isClearable
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
                href="/pipelines/new"
                className="btn btn-primary text-white mb-4"
                onClick={this.handleCreatePipeline}
              >
                Create pipeline
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
  addPipeline: addPipeline,
  fetchStreams: fetchStreams,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewPipeline);

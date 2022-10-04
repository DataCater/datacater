import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import { Copy } from "react-feather";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { addPipeline } from "../../actions/pipelines";
import { fetchStreams } from "../../actions/streams";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import "../../scss/fonts.scss";

class NewPipeline extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingPipelineFailed: false,
      errorMessages: {},
      showApiCall: false,
      pipeline: {
        metadata: {},
        spec: {},
      },
      pipelineCreated: false,
    };

    this.handleCreatePipeline = this.handleCreatePipeline.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
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

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
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
          <div className="col-12 mt-3">
            <div
              className="card welcome-card py-2"
              style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
            >
              <div className="card-body text-center p-0">
                <div className="row justify-content-center">
                  <div className="col-10 text-start">
                    <h4 className="fw-semibold mb-0">Create new pipeline</h4>
                    <p className="text-white mb-0">
                      Pipelines stream records between your Streams and can
                      apply filters and transforms on the way.
                    </p>
                  </div>
                  <div className="col-2 d-flex align-items-center justify-content-end">
                    <a
                      href="/pipelines/new"
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
                        href="https://docs.datacater.io/docs/api/pipelines/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light float-end"
                      >
                        See docs
                      </a>
                      <a
                        href="/pipelines/new/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light me-2 float-end"
                        onClick={(e) => {
                          e.preventDefault();
                          navigator.clipboard.writeText(
                            "curl " +
                              getApiPathPrefix(true) +
                              "/pipelines -XPOST -H'Content-Type:application/json' -H'Authorization:Bearer YOUR_TOKEN' -d'" +
                              JSON.stringify(this.state.pipeline) +
                              "'"
                          );
                        }}
                      >
                        <Copy className="feather-icon" />
                      </a>
                      <code className="text-white">
                        $ curl {getApiPathPrefix(true)}/pipelines/ \<br />
                        <span className="me-2"></span> -XPOST \<br />
                        <span className="me-2"></span>{" "}
                        -H&apos;Authorization:Bearer YOUR_TOKEN&apos; \<br />
                        <span className="me-2"></span>{" "}
                        -H&apos;Content-Type:application/json&apos; \<br />
                        <span className="me-2"></span> -d&apos;
                        {JSON.stringify(this.state.pipeline)}&apos;
                      </code>
                    </pre>
                  </div>
                )}
              </div>
            </div>
          </div>
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

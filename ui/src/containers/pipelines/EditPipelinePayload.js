import React, { Component } from "react";
import { Redirect } from "react-router-dom";
import { connect } from "react-redux";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { PayloadEditor } from "../../components/payload_editor/PayloadEditor";
import {
  fetchPipeline,
  updatePipeline,
  inspectPipeline,
} from "../../actions/pipelines";
import { inspectStream } from "../../actions/streams";
import { fetchFilters } from "../../actions/filters";
import { fetchTransforms } from "../../actions/transforms";
import "../../scss/grid.scss";
import "../../scss/grid/statistics.scss";

class EditPipelinePayload extends Component {
  constructor(props) {
    super(props);
    this.state = {
      unpersistedChanges: false,
      errorMessage: "",
      editorPipeline: "",
    };

    this.handleChange = this.handleChange.bind(this);
    this.getPipelineId = this.getPipelineId.bind(this);
    this.submitEditorContent = this.submitEditorContent.bind(this);
  }

  getPipelineId() {
    return this.props.match.params.id;
  }

  componentDidMount() {
    this.props.fetchPipeline(this.getPipelineId()).then(() => {
      const pipeline = this.props.pipelines.pipeline;

      // Load sample records from the source stream
      if (pipeline.metadata["stream-in"] !== undefined) {
        this.props.inspectStream(pipeline.metadata["stream-in"]);
      }

      this.setState({
        pipeline: pipeline,
        editorPipeline: JSON.stringify(pipeline, null, 2),
      });
    });

    this.props.fetchFilters();
    this.props.fetchTransforms();
  }

  handleChange(editorString) {
    this.setState({
      editorPipeline: editorString,
      unpersistedChanges: true,
    });
  }

  submitEditorContent(event) {
    event.preventDefault();
    let parsedPipeline = undefined;
    try {
      parsedPipeline = JSON.parse(this.state.editorPipeline, null, 2);
    } catch (syntaxError) {
      this.setState({
        errorMessage: syntaxError.message,
      });
    }

    if (this.state.unpersistedChanges && parsedPipeline !== undefined) {
      return this.props
        .updatePipeline(this.getPipelineId(), parsedPipeline)
        .then(() => {
          this.setState({
            unpersistedChanges: false,
            pipelineUpdated: true,
          });
        });
    } else {
      return Promise.resolve(undefined);
    }
  }

  render() {
    const pipeline = this.state.pipeline;
    if (pipeline == null) {
      return <></>;
    }

    const updatePipelineButtonDisabled = this.props.pipelines.updatingPipeline ? "disabled" : "";

    const updatePipelineButtonStyles = {
      width: "10em",
      pointerEvents: this.props.pipelines.updatingPipeline ? "none" : "auto",
    };

    const updatePipelineButtonContent = this.props.pipelines.updatingPipeline ? (
        <div className="spinner-border" role="status" style={{width: "1.5em", height: "1.5em"}}>
          <span className="visually-hidden">Loading...</span>
        </div>
    ) : "Edit pipeline";

    if (this.state.pipelineUpdated) {
      return <Redirect to={`/pipelines/${this.getPipelineId()}`} />;
    }

    const header = (
      <div className="row">
        <Breadcrumb
          items={[
            { name: "Pipelines", uri: "/pipelines" },
            { name: pipeline.uuid, uri: `/pipelines/${pipeline.uuid}` },
            { name: "EditPayload" },
          ]}
        />
        <div className="col-12 mt-3">
          <div
            className="card welcome-card py-2"
            style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
          >
            <div className="card-body text-center p-0">
              <div className="row justify-content-center">
                <div className="col-8 text-start d-flex align-items-center overflow-hidden text-nowrap">
                  <h4 className="fw-semibold mb-0">
                    {pipeline.name || "Untitled pipeline"}
                  </h4>
                </div>
                <div className="col-4 d-flex align-items-center justify-content-end"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );

    if (
      pipeline !== undefined &&
      pipeline.metadata !== undefined &&
      pipeline.metadata["stream-in"] === undefined
    ) {
      return (
        <div className="container">
          {header}
          <div className="alert alert-warning mt-4" role="alert">
            Please go to{" "}
            <a
              className="text-black"
              href={`/pipelines/${pipeline.uuid}/settings`}
            >
              Settings
            </a>{" "}
            and assign a source stream.
          </div>
        </div>
      );
    } else {
      return (
        <div className="container">
          {header}
          <div className="col-12 mt-4">
            <PayloadEditor
              apiPath="/pipelines/"
              code={this.state.editorPipeline}
              codeChange={this.handleChange}
            ></PayloadEditor>
            <div className="col-12 mt-4 mb-4">
              {![undefined, ""].includes(this.state.errorMessage) && (
                <div className="alert alert-danger mt-4">
                  <p className="h6 fs-bolder">API response:</p>
                  {this.state.errorMessage}
                </div>
              )}
              <a
                href={`/pipelines/${pipeline.uuid}/edit-payload`}
                className={`btn btn-primary text-white ${updatePipelineButtonDisabled}`}
                onClick={this.submitEditorContent}
                style={updatePipelineButtonStyles}
              >
                {updatePipelineButtonContent}
              </a>
            </div>
          </div>
        </div>
      );
    }
  }
}

const mapStateToProps = function (state) {
  return {
    filters: state.filters,
    pipelines: state.pipelines,
    streams: state.streams,
    transforms: state.transforms,
  };
};

const mapDispatchToProps = {
  fetchFilters: fetchFilters,
  fetchPipeline: fetchPipeline,
  fetchTransforms: fetchTransforms,
  inspectPipeline: inspectPipeline,
  inspectStream: inspectStream,
  updatePipeline: updatePipeline,
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(EditPipelinePayload);

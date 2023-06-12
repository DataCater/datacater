import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { addPipeline } from "../../actions/pipelines";
import { fetchStreams } from "../../actions/streams";
import "../../scss/fonts.scss";
import { PayloadEditor } from "../../components/payload_editor/PayloadEditor";

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
      payloadEditorChanges: false,
      showPayloadEditor: false,
      editorPipeline: "",
    };

    this.handleCreatePipeline = this.handleCreatePipeline.bind(this);
    this.handleChange = this.handleChange.bind(this);
    // payloadEditor specific functions
    this.loadHTMLForm = this.loadHTMLForm.bind(this);
    this.loadPayloadEditor = this.loadPayloadEditor.bind(this);
    this.toggleForm = this.toggleForm.bind(this);
    this.handleEditorChange = this.handleEditorChange.bind(this);
    this.submitForm = this.submitForm.bind(this);
    this.submitEditorContent = this.submitEditorContent.bind(this);
  }

  componentDidMount() {
    this.props.fetchStreams();
  }

  handleCreatePipeline(event) {
    event.preventDefault();

    if (this.state.showPayloadEditor) {
      this.submitEditorContent();
    } else {
      this.submitForm();
    }
  }

  submitForm() {
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

  submitEditorContent() {
    let parsedEditorStream = undefined;
    try {
      parsedEditorStream = JSON.parse(this.state.editorPipeline);
    } catch (syntaxError) {
      this.setState({
        pipelineCreated: false,
        errorMessage: syntaxError.message,
      });
    }

    if (parsedEditorStream !== undefined) {
      this.props.addPipeline(parsedEditorStream).then(() => {
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

  toggleForm(event) {
    event.preventDefault();
    let isShowingPayloadEditor = !this.state.showPayloadEditor;

    if (isShowingPayloadEditor) {
      this.setState({
        showPayloadEditor: isShowingPayloadEditor,
        editorPipeline: JSON.stringify(this.state.pipeline, null, 2),
      });
    } else if (
      this.state.payloadEditorChanges &&
      !window.confirm("Going back will reset all edits in the editor!")
    ) {
      this.setState({
        showPayloadEditor: true,
      });
    } else {
      this.setState({
        editorPipeline: JSON.stringify(this.state.pipeline, null, 2),
        showPayloadEditor: isShowingPayloadEditor,
        errorMessage: "",
        payloadEditorChanges: false,
      });
    }
  }

  handleEditorChange(value) {
    this.setState({
      editorPipeline: value,
      payloadEditorChanges: true,
    });
  }

  loadPayloadEditor() {
    return (
      <div className="col-12 mt-4">
        <PayloadEditor
          apiPath="/pipeline/"
          code={this.state.editorPipeline}
          codeChange={this.handleEditorChange}
        ></PayloadEditor>
      </div>
    );
  }

  loadHTMLForm() {
    const pipeline = this.state.pipeline;

    const streamOptions = this.props.streams.streams.map((stream) => {
      const name = `${stream.name || "Untitled stream"} (${stream.uuid})`;
      return { value: stream.uuid, label: name };
    });

    return (
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
      </form>
    );
  }

  render() {
    if (this.state.pipelineCreated) {
      return (
        <Redirect to={"/pipelines/" + this.props.pipelines.pipeline.uuid} />
      );
    }

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
          {this.state.showPayloadEditor
            ? this.loadPayloadEditor()
            : this.loadHTMLForm()}
          {![undefined, ""].includes(this.state.errorMessage) && (
            <div className="alert alert-danger mt-4">
              <p className="h6 fs-bolder">API response:</p>
              {this.state.errorMessage}
            </div>
          )}
          <div className="col-12 mt-4">
            <button
              className="btn btn-primary text-white"
              onClick={this.handleCreatePipeline}
            >
              Create pipeline
            </button>
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
    pipelines: state.pipelines,
    streams: state.streams,
  };
};

const mapDispatchToProps = {
  addPipeline: addPipeline,
  fetchStreams: fetchStreams,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewPipeline);

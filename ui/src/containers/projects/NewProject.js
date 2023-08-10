import React, { Component } from "react";
import { connect } from "react-redux";
import { PayloadEditor } from "../../components/payload_editor/PayloadEditor";
import { Redirect } from "react-router-dom";
import Creatable from "react-select/creatable";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { addProject } from "../../actions/projects";
import "../../scss/fonts.scss";

class NewProject extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingProjectFailed: false,
      payloadEditorChanges: false, // indicate, if the payload editor introduced changes
      payloadEditorCode: "", // payloadEditorCode as string, only valid during editing
      showPayloadEditor: false,
      errorMessages: {},
      editorProject: "",
      project: {
        name: "",
        spec: {},
      },
      projectCreated: false,
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleCreateProject = this.handleCreateProject.bind(this);
    // payload specific  functions
    this.loadHTMLForm = this.loadHTMLForm.bind(this);
    this.loadPayloadEditor = this.loadPayloadEditor.bind(this);
    this.toggleForm = this.toggleForm.bind(this);
    this.handleEditorChange = this.handleEditorChange.bind(this);
    this.submitForm = this.submitForm.bind(this);
    this.submitEditorContent = this.submitEditorContent.bind(this);
  }

  loadHTMLForm(project) {
    return (
      <form>
        <div className="col-12 mt-4">
          <label htmlFor="name" className="form-label">
            <h5 className="fw-semibold">
              Name<span className="text-danger ms-1">*</span>
            </h5>
            <span className="text-muted fs-7 me-2">
              Name of the Project context.
            </span>
          </label>
          <input
            type="text"
            className="form-control"
            id="name"
            name="name"
            onChange={this.handleChange}
            value={this.state.project["name"] || ""}
          />
        </div>
      </form>
    );
  }

  toggleForm(event) {
    event.preventDefault();
    let isShowingPayloadEditor = !this.state.showPayloadEditor;

    if (isShowingPayloadEditor) {
      this.setState({
        showPayloadEditor: isShowingPayloadEditor,
        editorProject: JSON.stringify(this.state.project, null, 2),
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
        editorProject: JSON.stringify(this.state.project, null, 2),
        showPayloadEditor: isShowingPayloadEditor,
        errorMessage: "",
        payloadEditorChanges: false,
      });
    }
  }

  handleEditorChange(value) {
    this.setState({
      editorProject: value,
      payloadEditorChanges: true,
    });
  }

  loadPayloadEditor() {
    return (
      <div className="col-12 mt-4">
        <PayloadEditor
          apiPath="/projects"
          code={this.state.editorProject}
          codeChange={this.handleEditorChange}
        ></PayloadEditor>
      </div>
    );
  }

  handleCreateProject(event) {
    event.preventDefault();
    if (this.state.showPayloadEditor) {
      this.submitEditorContent();
    } else {
      this.submitForm();
    }
  }

  submitEditorContent() {
    let parsedEditorProject = undefined;
    try {
      parsedEditorProject = JSON.parse(this.state.editorProject);
    } catch (syntaxError) {
      this.setState({
        projectCreated: false,
        errorMessage: syntaxError.message,
      });
    }

    if (parsedEditorProject !== null) {
      this.props.addProject(parsedEditorProject).then(() => {
        if (this.props.projects.errorMessage !== undefined) {
          this.setState({
            projectCreated: false,
            errorMessage: this.props.projects.errorMessage,
          });
        } else {
          this.setState({
            projectCreated: true,
            errorMessage: "",
          });
        }
      });
    }
  }

  submitForm() {
    this.props.addProject(this.state.project).then(() => {
      if (this.props.projects.errorMessage !== undefined) {
        this.setState({
          projectCreated: false,
          errorMessage: this.props.projects.errorMessage,
        });
      } else {
        this.setState({
          projectCreated: true,
          errorMessage: "",
        });
      }
    });
  }

  handleChange(event) {
    let project = this.state.project;

    const newValue = event.target.value;

    switch (event.target.dataset.prefix) {
      case "spec":
        project["spec"][event.target.name] = newValue;
        break;
      case "name":
        project["name"] = newValue;
        break;
      default:
        project[event.target.name] = newValue;
        break;
    }

    this.setState({
      creatingProjectFailed: false,
      errorMessage: "",
      project: project,
    });
  }

  render() {
    if (this.state.projectCreated) {
      return <Redirect to={"/home"} />;
    }

    const project = this.state.project;

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb items={[{ name: "Projects" }, { name: "New project" }]} />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/projects/"
            apiPath="/projects/"
            httpMethod="POST"
            requestBody={this.state.project}
            title="Create new project"
            subTitle="Projects provide a logical separation for your resources"
          />
          {this.state.showPayloadEditor
            ? this.loadPayloadEditor(project)
            : this.loadHTMLForm(project)}
          {![undefined, ""].includes(this.state.errorMessage) && (
            <div className="alert alert-danger mt-4">
              <p className="h6 fs-bolder">API response:</p>
              {this.state.errorMessage}
            </div>
          )}
          <div className="col-12 my-4">
            <button
              className="btn btn-primary text-white"
              onClick={this.handleCreateProject}
              disabled={this.props.projects.creatingProject}
              // Changed size to 10em, because it prevents button resize when displaying spinner + it is closest to the original size
              style={{
                width: "10em",
              }}
            >
              {this.props.projects.creatingProject ? (
                <div className="d-flex align-items-center">
                  <div
                    className="spinner-border me-2"
                    role="status"
                    style={{ width: "1.5em", height: "1.5em" }}
                  />
                  <span>Creating...</span>
                </div>
              ) : (
                "Create project"
              )}
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
    projects: state.projects,
  };
};

const mapDispatchToProps = {
  addProject: addProject,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewProject);

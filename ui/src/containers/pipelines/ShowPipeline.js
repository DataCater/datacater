import React, { Component } from "react";
import { connect } from "react-redux";
import { Settings, Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import yaml from "js-yaml";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { deletePipeline, fetchPipeline } from "../../actions/pipelines";
import { jsonToYaml } from "../../helpers/jsonToYaml";

class ShowPipeline extends Component {
  constructor(props) {
    super(props);
    this.state = {
      pipelineDeleted: false,
    };
    this.handleDelete = this.handleDelete.bind(this);
  }

  componentDidMount() {
    this.props.fetchPipeline(this.getPipelineId());
  }

  getPipelineId() {
    return this.props.match.params.id;
  }

  handleDelete(event) {
    event.preventDefault();

    if (window.confirm("Are you sure that you want to delete the pipeline?")) {
      this.props
        .deletePipeline(this.getPipelineId())
        .then(() => this.setState({ pipelineDeleted: true }));
    }
  }

  render() {
    if (this.state.pipelineDeleted) {
      return <Redirect to="/pipelines" />;
    }

    const pipeline = this.props.pipelines.pipeline;

    if (![undefined, ""].includes(this.props.pipelines.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.pipelines.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    if (pipeline === undefined) {
      return <div></div>;
    }

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Pipelines", uri: "/pipelines" },
              { name: pipeline.uuid },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/pipelines/"
            apiPath={`/pipelines/${pipeline.uuid}`}
            buttons={
              <>
                <a
                  href={`/pipelines/${pipeline.uuid}/edit-payload`}
                  className="btn btn-light btn-secondary ms-2"
                >
                  Edit as JSON
                </a>
                <a
                  href={`/pipelines/${pipeline.uuid}/edit`}
                  className="btn btn-primary text-white ms-2"
                >
                  Edit in Pipeline Designer
                </a>
                <a
                  href={`/pipelines/${pipeline.uuid}/settings`}
                  className="btn btn-light ms-2"
                >
                  <Settings className="feather-icon" />
                </a>
                <a
                  href={`/pipelines/${pipeline.uuid}`}
                  onClick={this.handleDelete}
                  className="btn btn-light btn-outline-danger ms-2"
                >
                  <Trash2 className="feather-icon" />
                </a>
              </>
            }
            title={pipeline.name || "Untitled pipeline"}
          />
        </div>
        <div className="row mt-4">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <SyntaxHighlighter
                  language="yaml"
                  showLineNumbers={true}
                  showInlineLineNumbers={true}
                  customStyle={{ marginBottom: "0px", background: "none" }}
                >
                  {jsonToYaml(pipeline)}
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
    pipelines: state.pipelines,
  };
};

const mapDispatchToProps = {
  deletePipeline: deletePipeline,
  fetchPipeline: fetchPipeline,
};

export default connect(mapStateToProps, mapDispatchToProps)(ShowPipeline);

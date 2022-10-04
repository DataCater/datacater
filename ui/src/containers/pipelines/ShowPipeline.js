import React, { Component } from "react";
import { connect } from "react-redux";
import { Copy, Settings, Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import * as YAML from "json-to-pretty-yaml";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import { deletePipeline, fetchPipeline } from "../../actions/pipelines";

class ShowPipeline extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showApiCall: false,
      pipelineDeleted: false,
    };
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
    this.handleDelete = this.handleDelete.bind(this);
  }

  componentDidMount() {
    this.props.fetchPipeline(this.getPipelineId());
  }

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
    });
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
          <div className="col-12 mt-3">
            <div
              className="card welcome-card py-2"
              style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
            >
              <div className="card-body text-center p-0">
                <div className="row justify-content-center">
                  <div className="col-6 text-start d-flex align-items-center">
                    <h4 className="fw-semibold mb-0">{pipeline.name}</h4>
                  </div>
                  <div className="col-6 d-flex align-items-center justify-content-end">
                    <div>
                      <a
                        href="/pipelines"
                        className="btn btn-light btn-pill"
                        onClick={this.toggleShowApiCall}
                      >
                        {this.state.showApiCall ? "Hide" : "Show"} API call
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
                    </div>
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
                        href="/pipelines/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light me-2 float-end"
                        onClick={(e) => {
                          e.preventDefault();
                          navigator.clipboard.writeText(
                            "curl " +
                              getApiPathPrefix(true) +
                              "/pipelines/" +
                              pipeline.uuid +
                              " -H'Authorization:Bearer YOUR_TOKEN'"
                          );
                        }}
                      >
                        <Copy className="feather-icon" />
                      </a>
                      <code className="text-white">
                        $ curl {getApiPathPrefix(true)}/pipelines/
                        {pipeline.uuid} \
                        <br />
                        <span className="me-2"></span>{" "}
                        -H&apos;Authorization:Bearer YOUR_TOKEN&apos;
                        <br />
                      </code>
                    </pre>
                  </div>
                )}
              </div>
            </div>
          </div>
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
                  {YAML.stringify(pipeline)}
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

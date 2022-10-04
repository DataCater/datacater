import React, { Component } from "react";
import { connect } from "react-redux";
import { Copy } from "react-feather";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { fetchPipelines } from "../../actions/pipelines";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";

class ListPipelines extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showApiCall: false,
    };
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
  }

  componentDidMount() {
    this.props.fetchPipelines();
  }

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
    });
  }

  render() {
    const pipelines = this.props.pipelines.pipelines;

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

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb items={[{ name: "Pipelines" }]} />
          <div className="col-12 mt-3">
            <div
              className="card welcome-card py-2"
              style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
            >
              <div className="card-body text-center p-0">
                <div className="row justify-content-center">
                  <div className="col-8 text-start">
                    <h4 className="fw-semibold mb-0">Pipelines</h4>
                    <p className="text-white mb-0">
                      Pipelines stream records between your Streams and can
                      apply filters and transforms on the way.
                    </p>
                  </div>
                  <div className="col-4 d-flex align-items-center justify-content-end">
                    <div>
                      <a
                        href="/pipelines"
                        className="btn btn-light btn-pill"
                        onClick={this.toggleShowApiCall}
                      >
                        {this.state.showApiCall ? "Hide" : "Show"} API call
                      </a>
                      {pipelines.length > 0 && (
                        <a
                          href="/pipelines/new"
                          className="btn btn-primary text-white ms-2"
                        >
                          Create new pipeline
                        </a>
                      )}
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
                              "/pipelines -H'Authorization:Bearer YOUR_TOKEN'"
                          );
                        }}
                      >
                        <Copy className="feather-icon" />
                      </a>
                      <code className="text-white">
                        $ curl {getApiPathPrefix(true)}/pipelines/ \<br />
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
            {pipelines.length === 0 && (
              <div className="card">
                <div className="card-body">
                  <div className="d-flex align-items-center justify-content-center m-5">
                    <a
                      href="/pipelines/new"
                      className="btn btn-lg text-white"
                      style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
                    >
                      Create your first pipeline
                    </a>
                  </div>
                </div>
              </div>
            )}
            {pipelines.length > 0 && (
              <div className="list-group">
                {pipelines.map((pipeline, idx) => (
                  <a
                    href={`/pipelines/${pipeline.uuid}`}
                    key={idx}
                    className="list-group-item list-group-item-action bg-white p-4"
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h5 className="d-flex align-items-center">
                        {pipeline.name || "Untitled pipeline"}
                      </h5>
                      <small className="d-flex align-items-center">
                        {pipeline.uuid}
                      </small>
                    </div>
                  </a>
                ))}
              </div>
            )}
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
  fetchPipelines: fetchPipelines,
};

export default connect(mapStateToProps, mapDispatchToProps)(ListPipelines);

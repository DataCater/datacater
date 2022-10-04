import React, { Component } from "react";
import { connect } from "react-redux";
import { Copy, Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import * as YAML from "json-to-pretty-yaml";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import { deleteStream, fetchStream } from "../../actions/streams";

class ShowStream extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showApiCall: false,
      streamDeleted: false,
    };
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
    this.handleDelete = this.handleDelete.bind(this);
  }

  componentDidMount() {
    this.props.fetchStream(this.getStreamId());
  }

  toggleShowApiCall(event) {
    event.preventDefault();

    this.setState({
      showApiCall: !this.state.showApiCall,
    });
  }

  getStreamId() {
    return this.props.match.params.id;
  }

  handleDelete(event) {
    event.preventDefault();

    if (window.confirm("Are you sure that you want to delete the stream?")) {
      this.props.deleteStream(this.getStreamId()).then(() => {
        this.setState({ streamDeleted: true });
      });
    }
  }

  render() {
    if (this.state.streamDeleted) {
      return <Redirect to="/streams" />;
    }

    const stream = this.props.streams.stream;

    if (![undefined, ""].includes(this.props.streams.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.streams.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    if (stream === undefined) {
      return <div></div>;
    }

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Streams", uri: "/streams" },
              { name: stream.uuid },
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
                    <h4 className="fw-semibold mb-0">{stream.name}</h4>
                  </div>
                  <div className="col-6 d-flex align-items-center justify-content-end">
                    <div>
                      <a
                        href="/streams"
                        className="btn btn-light btn-pill"
                        onClick={this.toggleShowApiCall}
                      >
                        {this.state.showApiCall ? "Hide" : "Show"} API call
                      </a>
                      <a
                        href={`/streams/${stream.uuid}/edit`}
                        className="btn btn-primary text-white ms-2"
                      >
                        Edit
                      </a>
                      <a
                        href={`/streams/${stream.uuid}/inspect`}
                        className="btn btn-light ms-2"
                      >
                        Inspect
                      </a>
                      <a
                        href={`/streams/${stream.uuid}`}
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
                        href="https://docs.datacater.io/docs/api/streams/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light float-end"
                      >
                        See docs
                      </a>
                      <a
                        href="/streams/"
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-sm btn-light me-2 float-end"
                        onClick={(e) => {
                          e.preventDefault();
                          navigator.clipboard.writeText(
                            "curl " +
                              getApiPathPrefix(true) +
                              "/streams/" +
                              stream.uuid +
                              " -H'Authorization:Bearer YOUR_TOKEN'"
                          );
                        }}
                      >
                        <Copy className="feather-icon" />
                      </a>
                      <code className="text-white">
                        $ curl {getApiPathPrefix(true)}/streams/{stream.uuid} \
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
                  {YAML.stringify(stream)}
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
    streams: state.streams,
  };
};

const mapDispatchToProps = {
  deleteStream: deleteStream,
  fetchStream: fetchStream,
};

export default connect(mapStateToProps, mapDispatchToProps)(ShowStream);

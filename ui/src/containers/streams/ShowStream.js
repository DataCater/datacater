import React, { Component } from "react";
import { connect } from "react-redux";
import { Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { deleteStream, fetchStream } from "../../actions/streams";
import { jsonToYaml } from "../../helpers/jsonToYaml";

class ShowStream extends Component {
  constructor(props) {
    super(props);
    this.state = {
      streamDeleted: false,
    };
    this.handleDelete = this.handleDelete.bind(this);
  }

  componentDidMount() {
    this.props.fetchStream(this.getStreamId());
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
          <Header
            apiDocs="https://docs.datacater.io/docs/api/streams/"
            apiPath={`/streams/${stream.uuid}`}
            buttons={
              <>
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
              </>
            }
            title={stream.name || "Untitled stream"}
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
                  {jsonToYaml(stream)}
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

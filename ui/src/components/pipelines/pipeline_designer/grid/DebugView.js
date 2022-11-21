import React, { Component } from "react";
import { LifeBuoy } from "react-feather";
import * as YAML from "json-to-pretty-yaml";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";

class DebugView extends Component {
  constructor(props) {
    super(props);
    this.state = {
      tab: "error",
    };
    this.goToTab = this.goToTab.bind(this);
  }

  goToTab(event) {
    event.preventDefault();

    this.setState({
      tab: event.target.dataset.tab,
    });
  }

  render() {
    const record = this.props.record;
    const pipeline = this.props.pipeline;
    const error = record.metadata["error"];

    let debugContent = "Not available.";

    if (this.state.tab === "error") {
      debugContent = JSON.stringify(error, null, 2);
    } else if (this.state.tab === "record") {
      let originalRecord = JSON.parse(JSON.stringify(record));
      delete originalRecord.metadata["error"];
      delete originalRecord.id;
      debugContent = JSON.stringify(originalRecord, null, 2);
    } else if (this.state.tab === "transform") {
      const match = /steps\[(\d+)\]\.fields\[(.+)\]/g.exec(
        error["location"]["path"]
      );
      if (match != null && match.length == 3) {
        const step = parseInt(match[1]);
        const fieldName = match[2];
        debugContent = YAML.stringify(
          pipeline.spec.steps[step].fields[fieldName]
        );
      } else {
        const match = /steps\[(\d+)\].*/g.exec(error["location"]["path"]);
        if (match != null && match.length == 2) {
          const step = parseInt(match[1]);
          debugContent = YAML.stringify(pipeline.spec.steps[step]);
        }
      }
    }

    return (
      <div className="modal d-block" tabIndex="-1">
        <div className="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title align-items-center d-flex">
                <LifeBuoy className="feather-icon me-2" /> Debug view for record
                #{record.id}
              </h5>
              <button
                type="button"
                className="btn-close"
                onClick={this.props.closeDebugViewFunc}
                aria-label="Close"
              ></button>
            </div>
            <div className="modal-body" style={{ height: "60vh" }}>
              <ul className="nav nav-pills">
                <li className="nav-item">
                  {this.state.tab === "error" && (
                    <a
                      className="nav-link active"
                      aria-current="error"
                      href="#"
                    >
                      Error
                    </a>
                  )}
                  {this.state.tab !== "error" && (
                    <a
                      onClick={this.goToTab}
                      data-tab="error"
                      className="nav-link"
                      href="#"
                    >
                      Error
                    </a>
                  )}
                </li>
                <li className="nav-item">
                  {this.state.tab === "record" && (
                    <a
                      className="nav-link active"
                      aria-current="record"
                      href="#"
                    >
                      Original record
                    </a>
                  )}
                  {this.state.tab !== "record" && (
                    <a
                      onClick={this.goToTab}
                      data-tab="record"
                      className="nav-link"
                      href="#"
                    >
                      Original record
                    </a>
                  )}
                </li>
                <li className="nav-item">
                  {this.state.tab === "transform" && (
                    <a
                      className="nav-link active"
                      aria-current="transform"
                      href="#"
                    >
                      Failed transform
                    </a>
                  )}
                  {this.state.tab !== "transform" && (
                    <a
                      onClick={this.goToTab}
                      data-tab="transform"
                      className="nav-link"
                      href="#"
                    >
                      Failed transform
                    </a>
                  )}
                </li>
              </ul>
              <SyntaxHighlighter
                language="yaml"
                showLineNumbers={true}
                showInlineLineNumbers={true}
                customStyle={{
                  fontSize: "10px",
                  marginBottom: "0px",
                  background: "none",
                }}
              >
                {debugContent}
              </SyntaxHighlighter>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={this.props.closeDebugViewFunc}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default DebugView;

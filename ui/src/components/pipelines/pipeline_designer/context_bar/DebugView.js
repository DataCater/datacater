import React, { Component } from "react";
import { LifeBuoy } from "react-feather";
import yaml from "js-yaml";
import { Button } from "react-bootstrap";
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
        debugContent = yaml.dump(pipeline.spec.steps[step].fields[fieldName]);
      } else {
        const match = /steps\[(\d+)\].*/g.exec(error["location"]["path"]);
        if (match != null && match.length == 2) {
          const step = parseInt(match[1]);
          debugContent = yaml.dump(pipeline.spec.steps[step]);
        }
      }
    }

    return (
      <React.Fragment>
        <div className="row py-4">
          <div className="col">
            <h4 className="mb-0 overflow-hidden text-nowrap d-flex align-items-center fw-bold">
              <LifeBuoy className="feather-icon me-2" />
              Debug view
            </h4>
          </div>
        </div>
        <div className="form-group mb-0 pb-4 datacater-context-bar-content">
          <ul className="nav nav-tabs mb-3">
            <li className="nav-item">
              {this.state.tab === "error" && (
                <a
                  className="nav-link active text-black fw-bold"
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
                  className="nav-link active text-black fw-bold"
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
                  className="nav-link active text-black fw-bold"
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
              fontSize: "0.8rem",
              margin: "0",
              background: "#f7fbf8",
            }}
          >
            {debugContent}
          </SyntaxHighlighter>
        </div>
        <div className="datacater-context-bar-button-group border-top d-flex align-items-center bg-white mx-n4 px-4 datacater-context-bar-fixed-element">
          <Button
            className="w-100 btn-outline-primary"
            onClick={this.props.hideContextBarFunc}
            variant="white"
          >
            Close sidebar
          </Button>
        </div>
      </React.Fragment>
    );
  }
}

export default DebugView;

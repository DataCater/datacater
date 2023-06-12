import React, { Component } from "react";
import { LifeBuoy } from "react-feather";
import { Button } from "react-bootstrap";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { jsonToYaml } from "../../../../helpers/jsonToYaml";

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
      // Get the step index and the field name from the location path of the
      // error object.
      //
      // Example location path: steps[1].fields[email]
      // In the example, the step index is `1` and the field name is `email`
      const match = /steps\[(\d+)\]\.fields\[(.+)\]/g.exec(
        error["location"]["path"]
      );
      if (match != null && match.length == 3) {
        const step = parseInt(match[1]);
        const fieldName = match[2];
        debugContent = jsonToYaml(pipeline.spec.steps[step].fields[fieldName]);
      } else {
        // If we fail reading the step index and the field name from the
        // location path, we should try only getting the step index.
        // This might be relevant for record-level transforms/filters
        // that are not applied to fields.
        //
        // Example location path: steps[1]
        // In the example, the step index is `1`
        const match = /steps\[(\d+)\].*/g.exec(error["location"]["path"]);
        if (match != null && match.length == 2) {
          const step = parseInt(match[1]);
          debugContent = jsonToYaml(pipeline.spec.steps[step]);
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
                <button
                  className="nav-link active text-black fw-bold"
                  aria-current="error"
                >
                  Error
                </button>
              )}
              {this.state.tab !== "error" && (
                <button
                  onClick={this.goToTab}
                  data-tab="error"
                  className="nav-link"
                >
                  Error
                </button>
              )}
            </li>
            <li className="nav-item">
              {this.state.tab === "record" && (
                <button
                  className="nav-link active text-black fw-bold"
                  aria-current="record"
                >
                  Original record
                </button>
              )}
              {this.state.tab !== "record" && (
                <button
                  onClick={this.goToTab}
                  data-tab="record"
                  className="nav-link"
                >
                  Original record
                </button>
              )}
            </li>
            <li className="nav-item">
              {this.state.tab === "transform" && (
                <button
                  className="nav-link active text-black fw-bold"
                  aria-current="transform"
                >
                  Failed transform
                </button>
              )}
              {this.state.tab !== "transform" && (
                <button
                  onClick={this.goToTab}
                  data-tab="transform"
                  className="nav-link"
                >
                  Failed transform
                </button>
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

import React, {Component} from "react";
import AceEditor from "react-ace";
import * as defaults from "./PayloadDefaults";
import {Copy, Play} from "react-feather";


class PayloadEditor extends Component {

  constructor(props) {
    super(props);
    this.state = {
      code: this.getDefault(),
      unsavedChanges: false
    };

    this.getDefault = this.getDefault.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.sendPayload = this.sendPayload.bind(this);
  }

  getDefault() {
    let jsonString = JSON.stringify(defaults.STREAM, null, 2);
    return jsonString;
  }

  handleChange(value, event) {
    const unsavedChanges = unsavedChanges || value !== this.state.code;
    this.setState({
      code: value,
      unsavedChanges: unsavedChanges,
    });
  }

  sendPayload() {
    // TODO include auto-forward

  }

  render() {
    const buttonClassNames = this.state.unsavedChanges
      ? "btn btn-sm d-flex align-items-center my-2 btn-primary-soft"
      : "btn btn-sm d-flex align-items-center my-2 btn-outline-primary-soft";

    return (
      <div className="bg-black text-start p-3 position-relative">
        <p className="position-absolute" style={{ right: "16px", top: "5px" }}>
          <a
            href="#"
            target="_blank"
            rel="noreferrer"
            className="btn btn-sm btn-light me-2"
            onClick={this.sendPayload}
          >
            <Play className="feather-icon" />
          </a>
        </p>
        <AceEditor
          placeholder=""
          mode="json"
          theme="xcode"
          className="" // TODO: work on layouting
          onLoad={(editor) => {
            editor.renderer.setPadding(10);
            editor.renderer.setScrollMargin(10);
          }}
          onChange={this.handleChange}
          fontSize={13}
          height="300px"
          showPrintMargin={true}
          showGutter={true}
          highlightActiveLine={true}
          value={this.state.code}
          enableLiveAutocompletion={false}
          tabSize={2}
        />
      </div>
    )
  }
}

export default PayloadEditor;

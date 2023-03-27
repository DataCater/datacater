import React, { Component } from "react";
import AceEditor from "react-ace";
import * as defaults from "./PayloadDefaults";
import { addStream } from "../../actions/streams";
import { connect } from "react-redux";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";

import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/theme-xcode";

export class PayloadEditor extends Component {
  constructor(props) {
    super(props);
    this.state = {
      code: JSON.stringify(props.code, null, 2),
      unsavedChanges: false,
      streamCreated: false,
      stream: props.stream,
    };

    this.getDefault = this.getDefault.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  getDefault() {
    const apiPath = this.props.apiPath;
    const stream = this.state.stream;
    return JSON.stringify(stream);
  }

  handleChange(value, event) {
    const unsavedChanges = unsavedChanges || value !== this.state.code;
    this.setState({
      code: value,
      unsavedChanges: unsavedChanges,
    });
  }

  render() {
    return (
      <div className="datacater-code-editor">
        <Row>
          <Col>
            <h5 className="fw-semibold mb-4">Edit payload as JSON</h5>
          </Col>
        </Row>
        <Row>
          <Col className="mb-2">
            <AceEditor
              placeholder=""
              mode="json"
              theme="xcode"
              className=""
              onLoad={(editor) => {
                editor.renderer.setPadding(10);
                editor.renderer.setScrollMargin(10);
              }}
              onChange={this.handleChange}
              fontSize={13}
              height="300px"
              width="100%"
              showPrintMargin={true}
              showGutter={true}
              highlightActiveLine={true}
              value={this.state.code}
              enableLiveAutocompletion={false}
              tabSize={2}
            />
          </Col>
        </Row>
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
  addStream: addStream,
};

export default connect(mapStateToProps, mapDispatchToProps)(PayloadEditor);

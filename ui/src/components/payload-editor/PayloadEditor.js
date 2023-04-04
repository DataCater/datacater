import React, {Component} from "react";
import AceEditor from "react-ace";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";

import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/theme-xcode";

export class PayloadEditor extends Component {
  constructor(props) {
    super(props);
    console.debug("How low does this go?");
    this.state = {
      didChange: false,
      newCode: undefined,
      editorCode: ""
    };

    this.didIntroduceChange = this.didIntroduceChange.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  didIntroduceChange() {
    if (this.state.didChange) {
      return this.state.newCode;
    } else {
      return undefined;
    }
  }

  handleChange(value) {
    // this.setState({
    //   didChange: true,
    //   newCode: value
    // })
    this.props.codeChange(value);
  }

  render() {
    let content = this.props.code;
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
              setOptions={{useWorker: false}}
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
              value={content}
              enableLiveAutocompletion={false}
              tabSize={2}
            />
          </Col>
        </Row>
      </div>
    );
  }
}

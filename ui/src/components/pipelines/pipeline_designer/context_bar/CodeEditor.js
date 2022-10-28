import React, { Component } from "react";
import AceEditor from "react-ace";
import { Button } from "react-bootstrap";
import { Play } from "react-feather";

import "ace-builds/src-noconflict/mode-python";
import "ace-builds/src-noconflict/snippets/python";
import "ace-builds/src-noconflict/theme-xcode";

import "../../../../scss/code-editor.scss";

class CodeEditor extends Component {
  constructor(props) {
    super(props);

    this.state = {
      code: undefined,
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleApply = this.handleApply.bind(this);
  }

  componentDidMount() {
    const defaultTransform = "def transform(value, row):\n  return value";

    if ([undefined, ""].includes(this.props.value)) {
      this.setState({ code: defaultTransform });
    } else {
      this.setState({ code: this.props.value });
    }
  }

  handleChange(value, event) {
    this.setState({ code: value });
  }

  handleApply(event) {
    event.preventDefault();

    this.props.handleChangeFunc(
      undefined,
      this.props.currentStep,
      this.props.fieldName,
      "code",
      this.state.code,
      "transformationConfig"
    );
  }

  render() {
    const { executionError, isExecutingTransformation } =
      this.props.previewState;

    return (
      <div className="datacater-code-editor border-start border-end border-top border-bottom border-grey">
        <div className="row py-2 px-3 border-bottom border-dark">
          <div className="col d-flex align-items-center font-weight-bold">
            Python® 3.10.6
          </div>
          <div className="col-auto">
            <Button
              className="d-flex align-items-center border-dark my-2"
              onClick={this.handleApply}
              size="sm"
              variant="white"
            >
              <Play className="feather-icon" />
              {!isExecutingTransformation && "Save & Run"}
              {isExecutingTransformation && "Running..."}
            </Button>
          </div>
        </div>
        {executionError !== undefined && (
          <div className="text-bg-danger font-size-sm p-3">
            {executionError}
          </div>
        )}
        <AceEditor
          placeholder=""
          mode="python"
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
      </div>
    );
  }
}

export default CodeEditor;

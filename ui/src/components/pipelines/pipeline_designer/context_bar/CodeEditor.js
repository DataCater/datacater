import React, { Component } from "react";
import AceEditor from "react-ace";
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
      unsavedChanges: false,
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleApply = this.handleApply.bind(this);
  }

  componentDidMount() {
    const defaultFieldTransform =
      "# field: Value of the field that the transform is applied to.\n" +
      "# record: The entire record as dict.\n" +
      "def transform(field, record: dict):\n" +
      "  # Return the processed field.\n" +
      "  return field";
    const defaultFieldFilter =
      "# field: Value of the field that the transform is applied to.\n" +
      "# record: The entire record as Python dict.\n" +
      "def filter(field, record: dict) -> bool:\n" +
      "  # Return whether the transform shall be applied/the record shall be processed or not.\n" +
      "  return True";
    const defaultRecordTransform =
      '# record["key"]: The key of the Apache Kafka record. Can be overwritten.\n' +
      '# record["value"]: The value of the Apache Kafka record. Can be overwritten.\n' +
      '# record["metadata"]: The metadata of the Apache Kafka record, e.g., the offset or the timestamp. Cannot be overwritten.\n' +
      "def transform(record: dict) -> dict:\n" +
      "  # Return the processed record.\n" +
      "  return record";
    const defaultRecordFilter =
      '# record["key"]: The key of the Apache Kafka record.\n' +
      '# record["value"]: The value of the Apache Kafka record.\n' +
      '# record["metadata"]: The metadata of the Apache Kafka record, e.g., the offset or the timestamp.\n' +
      "def filter(record: dict) -> bool:\n" +
      "  # Return whether the transform shall be applied/the record shall be processed or not.\n" +
      "  return True";

    let defaultTransform = defaultFieldTransform;
    if (
      this.props.transformStep.kind === "Record" &&
      this.props.funcType === "transform"
    ) {
      defaultTransform = defaultRecordTransform;
    } else if (
      this.props.transformStep.kind === "Record" &&
      this.props.funcType === "filter"
    ) {
      defaultTransform = defaultRecordFilter;
    } else if (this.props.funcType === "filter") {
      defaultTransform = defaultFieldFilter;
    }

    if ([undefined, ""].includes(this.props.value)) {
      this.setState({ code: defaultTransform });
    } else {
      this.setState({ code: this.props.value });
    }
  }

  handleChange(value, event) {
    const unsavedChanges = unsavedChanges || value !== this.state.code;
    this.setState({
      code: value,
      unsavedChanges: unsavedChanges,
    });
  }

  handleApply(event) {
    event.preventDefault();

    this.props.handleChangeFunc(
      undefined,
      this.props.currentStep,
      this.props.fieldName,
      "code",
      this.state.code,
      this.props.funcType
    );

    this.setState({
      unsavedChanges: false,
    });
  }

  render() {
    const buttonClassNames = this.state.unsavedChanges
      ? "btn btn-sm d-flex align-items-center my-2 btn-primary-soft"
      : "btn btn-sm d-flex align-items-center my-2 btn-outline-primary-soft";

    return (
      <div className="datacater-code-editor border-start border-end border-top border-bottom border-grey">
        <div className="row py-2 px-3 border-bottom border-dark">
          <div className="col d-flex align-items-center font-weight-bold">
            Python®
          </div>
          <div className="col-auto">
            <button className={buttonClassNames} onClick={this.handleApply}>
              <Play className="feather-icon" />
              Save &amp; run
            </button>
          </div>
        </div>
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

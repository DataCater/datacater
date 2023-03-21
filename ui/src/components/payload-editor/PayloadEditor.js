import React, {Component} from "react";
import AceEditor from "react-ace";
import * as defaults from "./PayloadDefaults";
import {Copy, Play} from "react-feather";
import {Redirect} from "react-router-dom";
import {addStream} from "../../actions/streams";
import {connect} from "react-redux";


class PayloadEditor extends Component {


  constructor(props) {
    super(props);
    this.state = {
      code: this.getDefault(),
      unsavedChanges: false,
      streamCreated: false,
      stream: defaults.getByString(props.apiPath)
    };

    this.getDefault = this.getDefault.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleCreateStream = this.handleCreateStream.bind(this);
  }

  getDefault() {
    const apiPath = this.props.apiPath;
    let jsonString = JSON.stringify(defaults.getByString(apiPath), null, 2);

    return jsonString;
  }

  handleChange(value, event) {
    const unsavedChanges = unsavedChanges || value !== this.state.code;
    this.setState({
      code: value,
      unsavedChanges: unsavedChanges,
    });
  }

  handleCreateStream(event) {
    event.preventDefault();

    this.props.addStream(this.state.stream).then(() => {
      if (this.props.streams.errorMessage !== undefined) {
        this.setState({
          streamCreated: false,
          errorMessage: this.props.streams.errorMessage,
        });
      } else {
        this.setState({
          streamCreated: true,
          errorMessage: "",
        });
      }
    });
  }

  render() {
    if (this.state.streamCreated) {
      return <Redirect to={"/streams/" + this.props.streams.stream.uuid} />;
    }

    return (
      <div className="bg-black text-start p-3 position-relative">
        <p className="position-absolute" style={{ right: "16px", top: "5px" }}>
          <a
            href="#"
            target="_blank"
            rel="noreferrer"
            className="btn btn-sm btn-light me-2"
            onClick={this.handleCreateStream}
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

const mapStateToProps = function (state) {
  return {
    streams: state.streams,
  };
};

const mapDispatchToProps = {
  addStream: addStream,
};

export default connect(mapStateToProps, mapDispatchToProps)(PayloadEditor);

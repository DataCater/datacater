import React, { Component } from "react";
import { connect } from "react-redux";
import { fetchInfo } from "../../actions/info";


class Footer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: "",
      errorMessages: {},
      info: {
        version: {},
        resources: {},
        contact: {},
      },
    };
  }

  componentDidMount() {
    this.props
      .fetchInfo()
      .then(() => this.setState({ info: this.props.info.info }));
  }

  render() {
      const info = this.state.info;

      if (info == null) {
        return <></>;
      }

    return (
        <div>
          <div>
              <p>version info:</p>
              <p>version: {info.version.version}</p>
              <p>base image: {info.version.baseImage}</p>
              <p>pipeline image: {info.version.pipelineImage}</p>
              <p>python-runner image: {info.version.pythonRunnerImage}</p>
          </div>
          <div>
              <p>documentation:</p>
              <a href="{info.resources.streams.documentationUrl}">streams</a>
              <a href="{info.resources.deployments.documentationUrl}">deployments</a>
              <a href="{info.resources.pipelines.documentationUrl}">pipelines</a>
              <a href="{info.resources.configs.documentationUrl}">configs</a>
          </div>
      </div>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    info: state.info,
  };
};

const mapDispatchToProps = {
  fetchInfo: fetchInfo,
};

export default connect(mapStateToProps, mapDispatchToProps)(Footer);

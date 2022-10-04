import React, { Component } from "react";

class LoadingPipelineDesigner extends Component {
  render() {
    return (
      <div className="loading-pipeline-designer">
        <div className="loader-text">
          <div className="spinner-border text-success" role="status">
            <span className="sr-only">Loading...</span>
          </div>
          <div className="loader-label">{this.props.status}</div>
        </div>
      </div>
    );
  }
}

export default LoadingPipelineDesigner;

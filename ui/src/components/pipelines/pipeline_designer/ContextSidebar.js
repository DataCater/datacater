import React, { Component } from "react";
import "../../../scss/context-bar.scss";

class ContextSidebar extends Component {
  render() {
    return (
      <nav className="datacater-context-bar navbar navbar-vertical fixed-right navbar-expand-md navbar-light p-0">
        <div className="container-fluid">
          <div className="collapse navbar-collapse mx-0">
            {this.props.children}
          </div>
        </div>
      </nav>
    );
  }
}

export default ContextSidebar;

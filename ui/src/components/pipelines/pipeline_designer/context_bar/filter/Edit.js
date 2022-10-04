import React, { Component } from "react";
import Attribute from "./Attribute";
import { Button } from "react-bootstrap";

class Edit extends Component {
  render() {
    return (
      <React.Fragment>
        <Attribute
          filter={this.props.filter}
          filters={this.props.filters}
          attribute={this.props.attribute}
          editColumn={this.props.editColumn}
          handleChangeFunc={this.props.handleChangeFunc}
        />

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

export default Edit;

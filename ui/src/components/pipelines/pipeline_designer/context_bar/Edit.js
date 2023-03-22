import React, { Component } from "react";
import Field from "./Field";
import Record from "./Record";
import { Button } from "react-bootstrap";

class Edit extends Component {
  render() {
    return (
      <React.Fragment>
        {this.props.transformStep.kind === "Field" && (
          <Field
            changeContextBarSizeFunc={this.props.changeContextBarSizeFunc}
            contextBarSize={this.props.contextBarSize}
            currentStep={this.props.currentStep}
            editColumn={this.props.editColumn}
            field={this.props.field}
            fields={this.props.fields}
            filters={this.props.filters}
            handleChangeFunc={this.props.handleChangeFunc}
            previewState={this.props.previewState}
            profile={this.props.profile}
            transformStep={this.props.transformStep}
            transforms={this.props.transforms}
          />
        )}

        {this.props.transformStep.kind === "Record" && (
          <Record
            changeContextBarSizeFunc={this.props.changeContextBarSizeFunc}
            contextBarSize={this.props.contextBarSize}
            currentStep={this.props.currentStep}
            editColumn={this.props.editColumn}
            field={this.props.field}
            fields={this.props.fields}
            filters={this.props.filters}
            handleChangeFunc={this.props.handleChangeFunc}
            previewState={this.props.previewState}
            profile={this.props.profile}
            transformStep={this.props.transformStep}
            transforms={this.props.transforms}
          />
        )}

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

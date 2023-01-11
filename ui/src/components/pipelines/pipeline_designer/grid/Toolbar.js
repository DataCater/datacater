import React, { Component } from "react";
import { Edit2, Code, Package, Settings, Table, Trash2 } from "react-feather";

class Toolbar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      tempInspectLimit: undefined,
    };

    this.updateTempInspectLimit = this.updateTempInspectLimit.bind(this);
    this.removeStep = this.removeStep.bind(this);
  }

  updateTempInspectLimit(event) {
    if (!isNaN(event.target.value)) {
      this.setState({
        tempInspectLimit: event.target.value,
      });
    }
  }

  truncateText(text, characterLimit) {
    if (text !== undefined) {
      let truncatedText = text.substring(
        0,
        Math.min(characterLimit, text.length)
      );

      if (truncatedText.length < text.length) {
        truncatedText = truncatedText + "...";
      }

      return truncatedText;
    } else {
      return text;
    }
  }

  removeStep(event) {
    event.preventDefault();
    event.stopPropagation(); // stops the browser from redirecting.

    if (window.confirm("Are you sure that you want to delete this step?")) {
      this.props.removeStepFunc(this.props.currentStep);
    }
  }

  render() {
    const {
      hideStepNameFormFunc,
      inspectLimit,
      pipeline,
      sampleRecords,
      showGrid,
      showStepNameForm,
      showStepNameFormFunc,
      step,
      streamInspectLength,
      toggleShowGridFunc,
      toggleShowSettingsFunc,
      updateInspectLimitFunc,
      updateStepNameFunc,
    } = this.props;

    const gridButtonClassNames = showGrid
      ? "btn btn-sm btn-primary text-white"
      : "btn btn-sm btn-outline-primary";

    const rawButtonClassNames = !showGrid
      ? "btn btn-sm btn-primary text-white"
      : "btn btn-sm btn-outline-primary";

    let transformation = undefined;
    if (
      step !== undefined &&
      step.kind === "Record" &&
      step.transform !== undefined
    ) {
      const transformationKey = step.transform.key;
      transformation = this.props.transforms.find(
        (transformation) => transformation.key === transformationKey
      );
    }

    let filter = undefined;
    if (
      step !== undefined &&
      step.kind === "Record" &&
      step.filter !== undefined
    ) {
      const filterKey = step.filter.key;
      filter = this.props.filters.find((filter) => filter.key === filterKey);
    }

    return (
      <div className="container mb-2">
        <div className="row align-items-center">
          {step !== undefined && (
            <div className="col-auto d-flex align-items-center">
              {!showStepNameForm && (
                <>
                  <span
                    className="ms-1 fw-semibold clickable"
                    onClick={showStepNameFormFunc}
                  >
                    {this.truncateText(step.name, 35) || <i>Untitled step</i>}
                  </span>
                  <Edit2
                    className="feather-icon clickable ms-2"
                    onClick={showStepNameFormFunc}
                  />
                </>
              )}
              {showStepNameForm && (
                <input
                  autoFocus
                  type="text"
                  className="form-control form-control-sm d-inline float-left"
                  onBlur={hideStepNameFormFunc}
                  onKeyDown={(e) => {
                    if (e.which === 13) {
                      e.target.blur();
                    }
                  }}
                  onChange={updateStepNameFunc}
                  placeholder="Provide a name for this step"
                  value={step.name || ""}
                  style={{ width: "220px" }}
                />
              )}
            </div>
          )}
          <div className="col d-flex align-items-center justify-content-end">
            {step !== undefined &&
              step.kind === "Record" &&
              transformation === undefined &&
              filter === undefined && (
                <button
                  onClick={(e) => {
                    this.props.editColumnFunc();
                  }}
                  className="btn btn-primary-soft btn-sm btn-pill btn-preview-settings fw-semibold"
                >
                  <Package className="feather-icon me-1" />
                  Apply transform or filter to records
                </button>
              )}
            {step !== undefined &&
              step.kind === "Record" &&
              (transformation !== undefined || filter !== undefined) && (
                <button
                  onClick={(e) => {
                    this.props.editColumnFunc();
                  }}
                  className="btn btn-primary text-white btn-sm btn-pill btn-preview-settings fw-semibold"
                >
                  <Package className="feather-icon me-1" />
                  {transformation !== undefined &&
                    `Record-level transform: ${this.truncateText(
                      transformation.name,
                      25
                    )}`}
                  {transformation === undefined &&
                    `Record-level filter: ${this.truncateText(
                      filter.name,
                      25
                    )}`}
                </button>
              )}
            {step !== undefined && (
              <button
                className="btn btn-sm btn-pill btn-preview-settings btn-danger-soft ms-4"
                onClick={this.removeStep}
              >
                <Trash2 className="feather-icon me-1" />
                Delete step
              </button>
            )}
            <span className="mx-4">
              {streamInspectLength !== undefined &&
                sampleRecords.length < streamInspectLength && (
                  <>{sampleRecords.length} of </>
                )}
              {streamInspectLength} records
            </span>
            <button
              className="btn btn-sm btn-pill btn-primary-soft btn-preview-settings"
              onClick={toggleShowSettingsFunc}
            >
              <Settings className="feather-icon me-1" />
              Preview settings
            </button>
          </div>
        </div>
      </div>
    );
  }
}

export default Toolbar;

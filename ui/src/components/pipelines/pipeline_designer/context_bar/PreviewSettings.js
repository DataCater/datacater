import React, { Component } from "react";
import { Button } from "react-bootstrap";
import { Check, Code, Eye, EyeOff, Settings, Table } from "react-feather";

class PreviewSettings extends Component {
  constructor(props) {
    super(props);
    this.state = {
      tempInspectLimit: undefined,
    };

    this.updateTempInspectLimit = this.updateTempInspectLimit.bind(this);
  }

  updateTempInspectLimit(event) {
    if (!isNaN(event.target.value)) {
      this.setState({
        tempInspectLimit: event.target.value,
      });
    }
  }

  render() {
    const {
      hideFilteredOutRecords,
      inspectLimit,
      pipeline,
      showGrid,
      toggleHideFilteredOutRecordsFunc,
      toggleShowGridFunc,
      updateInspectLimitFunc,
    } = this.props;

    const gridButtonClassNames = showGrid
      ? "btn btn-sm w-50 btn-primary-soft"
      : "btn btn-sm w-50 btn-outline-primary-soft";

    const rawButtonClassNames = !showGrid
      ? "btn btn-sm w-50 btn-primary-soft"
      : "btn btn-sm w-50 btn-outline-primary-soft";

    const hideFilteredOutRecordsButtonClassNames = hideFilteredOutRecords
      ? "btn btn-sm w-50 btn-primary-soft"
      : "btn btn-sm w-50 btn-outline-primary-soft";

    const showFilteredOutRecordsButtonClassNames = !hideFilteredOutRecords
      ? "btn btn-sm w-50 btn-primary-soft"
      : "btn btn-sm w-50 btn-outline-primary-soft";

    return (
      <React.Fragment>
        <div className="row py-4">
          <div className="col">
            <h4 className="mb-0 overflow-hidden text-nowrap d-flex align-items-center fw-bold">
              <Settings className="feather-icon me-2" />
              Preview settings
            </h4>
          </div>
        </div>
        <div className="form-group mb-0 py-4 datacater-context-bar-content border-top">
          <div className="mb-4">
            <h6 className="fw-bold">Sample size</h6>
            <small className="text-muted">
              The number of records that are consumed from your source stream
              and used for previewing your pipeline (default: 100).
            </small>
            <div className="mt-3">
              <input
                type="text"
                className="form-control form-control-sm d-inline me-2"
                onChange={this.updateTempInspectLimit}
                aria-describedby="passwordHelpInline"
                onKeyDown={(e) => {
                  if (e.which === 13) {
                    // Enter key
                    updateInspectLimitFunc(this.state.tempInspectLimit);
                  }
                }}
                placeholder="100"
                value={
                  this.state.tempInspectLimit !== undefined
                    ? this.state.tempInspectLimit
                    : inspectLimit
                }
                style={{ width: "75px" }}
              />
              <button
                className="btn btn-sm btn-outline-primary-soft"
                onClick={() => {
                  updateInspectLimitFunc(this.state.tempInspectLimit);
                }}
              >
                Save &amp; reload samples
              </button>
            </div>
          </div>
          <div className="mb-4">
            <h6 className="fw-bold">Display mode</h6>
            <div className="btn-group w-100 mt-2">
              <a
                href={`/pipelines/${pipeline.uuid}/edit`}
                onClick={toggleShowGridFunc}
                className={gridButtonClassNames}
              >
                <h6 className="mt-2 clearfix">
                  <span className="float-start d-flex align-items-center ms-n1">
                    <Table className="feather-icon me-1" /> Grid
                  </span>
                  {showGrid && (
                    <span className="float-end">
                      <Check className="feather-icon" />
                    </span>
                  )}
                </h6>
                <p className="text-start">
                  Shows the values of the sample records in a spreadsheet.
                </p>
              </a>
              <a
                href={`/pipelines/${pipeline.uuid}/edit`}
                onClick={toggleShowGridFunc}
                className={rawButtonClassNames}
              >
                <h6 className="mt-2 clearfix">
                  <span className="float-start d-flex align-items-center ms-n1">
                    <Code className="feather-icon me-1" /> Raw
                  </span>
                  {!showGrid && (
                    <span className="float-end">
                      <Check className="feather-icon" />
                    </span>
                  )}
                </h6>
                <p className="text-start">
                  Shows the complete sample records, including their values,
                  their keys, and their metadata, in a text view.
                </p>
              </a>
            </div>
          </div>
          <div className="mb-4">
            <h6 className="fw-bold">Filtered out records</h6>
            <div className="btn-group w-100 mt-2">
              <a
                href={`/pipelines/${pipeline.uuid}/edit`}
                onClick={toggleHideFilteredOutRecordsFunc}
                className={hideFilteredOutRecordsButtonClassNames}
              >
                <h6 className="mt-2 clearfix">
                  <span className="float-start d-flex align-items-center ms-n1">
                    <EyeOff className="feather-icon me-1" /> Hide
                  </span>
                  {hideFilteredOutRecords && (
                    <span className="float-end">
                      <Check className="feather-icon" />
                    </span>
                  )}
                </h6>
                <p className="text-start">
                  Hide records that have been filtered out by the pipeline.
                </p>
              </a>
              <a
                href={`/pipelines/${pipeline.uuid}/edit`}
                onClick={toggleHideFilteredOutRecordsFunc}
                className={showFilteredOutRecordsButtonClassNames}
              >
                <h6 className="mt-2 clearfix">
                  <span className="float-start d-flex align-items-center ms-n1">
                    <Eye className="feather-icon me-1" /> Show
                  </span>
                  {!hideFilteredOutRecords && (
                    <span className="float-end">
                      <Check className="feather-icon" />
                    </span>
                  )}
                </h6>
                <p className="text-start">
                  Show records that have been filtered out by the previewed step
                  and highlight them in gray.
                </p>
              </a>
            </div>
          </div>
        </div>
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

export default PreviewSettings;

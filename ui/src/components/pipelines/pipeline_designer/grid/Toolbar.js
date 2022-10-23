import React, { Component } from "react";
import { Code, Table } from "react-feather";

class Toolbar extends Component {
  render() {
    const {
      hideStepNameFormFunc,
      pipeline,
      sampleRecords,
      showGrid,
      showStepNameForm,
      showStepNameFormFunc,
      step,
      toggleShowGridFunc,
      updateStepNameFunc,
    } = this.props;

    const gridButtonClassNames = showGrid
      ? "btn btn-sm btn-primary text-white"
      : "btn btn-sm btn-outline-primary";

    const rawButtonClassNames = !showGrid
      ? "btn btn-sm btn-primary text-white"
      : "btn btn-sm btn-outline-primary";

    return (
      <div className="container mb-2">
        <div className="row align-items-center">
          {step !== undefined && (
            <div className="col">
              {!showStepNameForm && (
                <>
                  <span className="ms-1 fw-semibold">
                    {step.name || <i>Untitled step</i>}
                  </span>
                  <button
                    className="btn btn-sm btn-outline-primary ms-3"
                    onClick={showStepNameFormFunc}
                  >
                    Edit
                  </button>
                </>
              )}
              {showStepNameForm && (
                <input
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
              <span class="badge text-primary fw-semibold me-4" style={{"backgroundColor": "#eaf6ec"}}>Kind: {step.kind}</span>
            }
            <div className="btn-group">
              <a
                href={`/pipelines/${pipeline.uuid}/edit`}
                onClick={toggleShowGridFunc}
                className={gridButtonClassNames}
              >
                <Table className="feather-icon" /> Grid
              </a>
              <a
                href={`/pipelines/${pipeline.uuid}/edit`}
                onClick={toggleShowGridFunc}
                className={rawButtonClassNames}
              >
                <Code className="feather-icon" /> Raw
              </a>
            </div>
            <span className="ms-4">{sampleRecords.length} records</span>
          </div>
        </div>
      </div>
    );
  }
}

export default Toolbar;

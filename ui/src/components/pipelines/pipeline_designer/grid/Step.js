import React, { Component } from "react";

class Step extends Component {
  getStepShortName(step, sortPosition) {
    let shortName = sortPosition;

    if ([undefined, ""].includes(step.name)) {
      return shortName;
    }

    const words = step.name.split(" ");

    if (words[0] !== undefined && words[0].length > 0) {
      shortName = words[0][0].toUpperCase()[0];

      if (words[1] !== undefined && words[1].length > 0) {
        shortName += words[1][0].toUpperCase()[0];
      }
    }

    return shortName;
  }

  getStepName(step) {
    if (step.name === undefined || step.name.length === 0) {
      return "Untitled Step";
    } else {
      return step.name;
    }
  }

  render() {
    const { sortPosition, step } = this.props;

    let classNames = "list-group-item border-0 p-0 me-3";
    if (
      this.props.currentStep === sortPosition &&
      !this.props.showDataSinkDialog
    ) {
      classNames += " active-step";
    }

    const activeDropZone =
      ![this.props.draggingItem, this.props.draggingOverItem].includes(
        undefined
      ) &&
      this.props.draggingOverItem === sortPosition &&
      ![sortPosition, sortPosition + 1].includes(this.props.draggingItem);

    return (
      <React.Fragment>
        <li
          className={classNames}
          data-sort-position={sortPosition}
          onDragLeave={this.props.onDragLeaveFunc}
          onDragOver={(event) => {
            this.props.onDragOverFunc(event, sortPosition);
          }}
          onDrop={this.props.onDropFunc}
        >
          <div
            className="avatar avatar-sm clickable d-flex align-items-center justify-content-center"
            data-name={this.getStepName(step)}
            data-short-name={this.getStepShortName(step, sortPosition)}
            data-sort-position={sortPosition}
            draggable={true}
            onDragEnd={this.props.onDragEndFunc}
            onDragStart={this.props.onDragStartFunc}
            onClick={(event) => {
              this.props.moveToStepFunc(event, sortPosition);
            }}
          >
            <div
              className="avatar-title bg-primary-soft rounded-circle text-primary"
              data-sort-position={sortPosition}
            >
              {this.getStepShortName(step, sortPosition)}
            </div>
          </div>
        </li>
        {activeDropZone && (
          <li className="list-group-item border-0 p-0 me-3 dragged-step">
            <div className="avatar avatar-sm d-flex align-items-center justify-content-center">
              <div className="avatar-title bg-primary-soft rounded-circle text-primary droppable">
                {this.props.draggingShortName}
              </div>
            </div>
          </li>
        )}
      </React.Fragment>
    );
  }
}

export default Step;

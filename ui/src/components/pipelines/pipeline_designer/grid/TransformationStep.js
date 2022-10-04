import React, { Component } from "react";

class TransformationStep extends Component {
  getTransformationStepShortName(transformationStep, sortPosition) {
    let shortName = sortPosition;

    if ([undefined, ""].includes(transformationStep.name)) {
      return shortName;
    }

    const words = transformationStep.name.split(" ");

    if (words[0] !== undefined && words[0].length > 0) {
      shortName = words[0][0].toUpperCase()[0];

      if (words[1] !== undefined && words[1].length > 0) {
        shortName += words[1][0].toUpperCase()[0];
      }
    }

    return shortName;
  }

  getTransformationStepName(transformationStep) {
    if (
      transformationStep.name === undefined ||
      transformationStep.name.length === 0
    ) {
      return "Untitled Step";
    } else {
      return transformationStep.name;
    }
  }

  render() {
    const { sortPosition, transformationStep } = this.props;

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
      this.props.draggingOverItem === transformationStep.sortPosition &&
      ![
        transformationStep.sortPosition,
        transformationStep.sortPosition + 1,
      ].includes(this.props.draggingItem);

    return (
      <React.Fragment>
        <li
          className={classNames}
          data-sort-position={transformationStep.sortPosition}
          onDragLeave={this.props.onDragLeaveFunc}
          onDragOver={(event) => {
            this.props.onDragOverFunc(event, transformationStep.sortPosition);
          }}
          onDrop={this.props.onDropFunc}
        >
          <div
            className="avatar avatar-sm clickable d-flex align-items-center justify-content-center"
            data-name={this.getTransformationStepName(transformationStep)}
            data-short-name={this.getTransformationStepShortName(
              transformationStep,
              sortPosition
            )}
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
              {this.getTransformationStepShortName(
                transformationStep,
                sortPosition
              )}
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

export default TransformationStep;

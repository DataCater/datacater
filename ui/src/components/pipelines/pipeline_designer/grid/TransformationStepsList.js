import React, { Component } from "react";
import { Button, Modal } from "react-bootstrap";
import { Plus, Trash2 } from "react-feather";
import TransformationStep from "./TransformationStep";
import "../../../../scss/designer/pipeline-steps.scss";

class TransformationStepsList extends Component {
  constructor(props) {
    super(props);
    this.state = {
      draggingItem: undefined,
      draggingName: undefined,
      draggingShortName: undefined,
      draggingOverItem: undefined,
      isHoveringDeleteZone: false,
      showDeleteModal: false,
      toBeDeletedStep: undefined,
    };

    this.onDragStart = this.onDragStart.bind(this);
    this.onDragEnd = this.onDragEnd.bind(this);
    this.onDragOver = this.onDragOver.bind(this);
    this.onDragLeave = this.onDragLeave.bind(this);
    this.onDrop = this.onDrop.bind(this);
    this.deleteDraggingItem = this.deleteDraggingItem.bind(this);
    this.enterDeleteZone = this.enterDeleteZone.bind(this);
    this.leaveDeleteZone = this.leaveDeleteZone.bind(this);
    this.toggleDeleteModal = this.toggleDeleteModal.bind(this);
    this.deleteTransformationStep = this.deleteTransformationStep.bind(this);
  }

  onDragStart(event) {
    // we need to set transferred data for Firefox, uargs!
    event.dataTransfer.setData("Text", this.id);
    this.setState({
      draggingItem: parseInt(event.target.dataset.sortPosition),
      draggingName: event.target.dataset.name,
      draggingShortName: event.target.dataset.shortName,
    });
  }

  onDragEnd(event) {
    event.preventDefault();
    this.setState({
      draggingItem: undefined,
      draggingName: undefined,
      draggingShortName: undefined,
    });
  }

  onDragOver(event, sortPosition) {
    event.preventDefault();
    if (this.state.draggingOverItem === undefined) {
      this.setState({
        draggingOverItem: sortPosition,
      });
    }
  }

  onDragLeave(event) {
    event.preventDefault();
    this.setState({
      draggingOverItem: undefined,
    });
  }

  onDrop(event) {
    event.preventDefault();
    event.stopPropagation(); // stops the browser from redirecting.

    if (
      this.state.draggingItem !== undefined &&
      this.state.draggingItem !== this.state.draggingOverItem &&
      this.state.draggingItem !== this.state.draggingOverItem + 1
    ) {
      this.props.moveTransformStepFunc(
        this.state.draggingItem,
        this.state.draggingOverItem + 1
      );
      this.setState({
        draggingItem: undefined,
        draggingOverItem: undefined,
      });
    }
  }

  deleteDraggingItem(event) {
    event.preventDefault();
    event.stopPropagation(); // stops the browser from redirecting.

    const draggingItem = this.state.draggingItem;
    if (draggingItem !== undefined) {
      if (
        window.confirm(
          "Are you sure that you want to delete the transform step?"
        )
      ) {
        this.props.removeTransformStepFunc(draggingItem);
        this.setState({
          draggingItem: undefined,
          draggingOverItem: undefined,
        });
      }
    }
  }

  enterDeleteZone(event) {
    event.preventDefault();

    this.setState({
      isHoveringDeleteZone: true,
    });
  }

  leaveDeleteZone(event) {
    event.preventDefault();

    this.setState({
      isHoveringDeleteZone: false,
    });
  }

  componentDidUpdate() {
    const listClassName =
      "datacater-pipeline-designer-pipeline-steps-list-items";
    // scroll to the right end of the steps list
    if (
      document.getElementById(listClassName) != null &&
      this.props.currentStep === this.props.transformationSteps.length &&
      this.state.draggingItem === undefined
    ) {
      document.getElementById(listClassName).scrollLeft =
        document.getElementById(listClassName).scrollWidth + 200;
    }
  }

  toggleDeleteModal() {
    this.setState({
      showDeleteModal: !this.state.showDeleteModal,
    });
  }

  deleteTransformationStep() {
    this.props.removeTransformStepFunc(this.state.toBeDeletedStep);
  }

  render() {
    let deleteZoneClassNames =
      "datacater-pipeline-designer-pipeline-steps-list-delete-zone d-flex align-items-center justify-content-center";
    if (this.state.isHoveringDeleteZone) {
      deleteZoneClassNames += " hovering";
    }

    return (
      <div className="container px-3 datacater-pipeline-designer-third-row datacater-pipeline-designer-pipeline-steps-list d-flex align-items-center overflow-hidden text-nowrap">
        {this.state.draggingItem !== undefined && (
          <div
            className={deleteZoneClassNames}
            onDragOver={this.enterDeleteZone}
            onDragLeave={this.leaveDeleteZone}
            onDrop={this.deleteDraggingItem}
          >
            <h6 className="mb-0 d-flex align-items-center justify-content-center">
              <Trash2 className="feather-icon" />
              <span className="ms-2">Drop here to delete</span>
            </h6>
          </div>
        )}
        <div
          className="h-75 d-flex align-items-center pe-4"
          onDragLeave={this.onDragLeave}
          onDragOver={(event) => {
            this.onDragOver(event, 0);
          }}
          onDrop={this.onDrop}
        >
          <h6 className="mb-0">Transformation steps</h6>
        </div>
        <ul
          id="datacater-pipeline-designer-pipeline-steps-list-items"
          className="list-group list-group-horizontal d-flex align-items-center ps-0"
        >
          {this.state.draggingOverItem === 0 && (
            <li className="list-group-item border-0 p-0 me-3 dragged-step">
              <div className="avatar avatar-sm d-flex align-items-center justify-content-center">
                <div className="avatar-title bg-primary-soft text-primary droppable">
                  {this.state.draggingShortName}
                </div>
              </div>
            </li>
          )}
          {this.props.transformationSteps.map((transformationStep, idx) => (
            <TransformationStep
              currentStep={this.props.currentStep}
              draggingItem={this.state.draggingItem}
              draggingName={this.state.draggingName}
              draggingShortName={this.state.draggingShortName}
              draggingOverItem={this.state.draggingOverItem}
              key={idx}
              moveToStepFunc={this.props.moveToStepFunc}
              onDragEndFunc={this.onDragEnd}
              onDragStartFunc={this.onDragStart}
              onDragLeaveFunc={this.onDragLeave}
              onDragOverFunc={this.onDragOver}
              onDropFunc={this.onDrop}
              sortPosition={idx}
              transformationStep={transformationStep}
              removeTransformationStepFunc={this.props.removeTransformStepFunc}
            />
          ))}
          <li className="list-group-item border-0 p-0">
            <div className="avatar avatar-sm d-flex align-items-center justify-content-center">
              <div className="avatar-title font-size-lg bg-primary-soft rounded-circle text-primary">
                <Plus
                  className="feather-icon clickable"
                  onClick={this.props.addTransformStepFunc}
                />
              </div>
            </div>
          </li>
        </ul>
      </div>
    );
  }
}

export default TransformationStepsList;

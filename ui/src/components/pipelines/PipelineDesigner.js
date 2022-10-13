import React, { Component } from "react";
import ContextSidebar from "./pipeline_designer/ContextSidebar";
import EditFilter from "./pipeline_designer/context_bar/filter/Edit";
import EditTransformation from "./pipeline_designer/context_bar/transformation/Edit";
import TransformationStepsList from "./pipeline_designer/grid/TransformationStepsList";
import Grid from "./pipeline_designer/Grid";

class PipelineDesigner extends Component {
  constructor(props) {
    super(props);
    this.state = {
      draggingItem: undefined,
      draggingName: undefined,
      draggingShortName: undefined,
      draggingOverItem: undefined,
    };
    this.onDragStart = this.onDragStart.bind(this);
    this.onDragEnd = this.onDragEnd.bind(this);
    this.onDragOver = this.onDragOver.bind(this);
    this.onDragLeave = this.onDragLeave.bind(this);
    this.onDrop = this.onDrop.bind(this);
    this.renderTransformationStepHeader =
      this.renderTransformationStepHeader.bind(this);
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

  onDragOver(event) {
    event.preventDefault();
    if (this.state.draggingOverItem === undefined) {
      this.setState({
        draggingOverItem: parseInt(event.target.dataset.sortPosition),
      });
    }
  }

  onDragLeave(event) {
    event.preventDefault();
    this.setState({ draggingOverItem: undefined });
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

  componentDidUpdate() {
    // scroll to the right end of the steps list
    if (
      document.getElementById("steps-list") != null &&
      this.props.currentStep === this.props.pipeline.pipelineSteps.length &&
      this.state.draggingItem === undefined
    ) {
      document.getElementById("steps-list").scrollLeft =
        document.getElementById("steps-list").scrollWidth + 200;
    }
  }

  renderTransformationStepHeader() {
    const pipelineSteps = this.props.pipeline.pipelineSteps.length;
    if (pipelineSteps === 0) {
      return "Transformation Steps";
    } else if (pipelineSteps === 1) {
      return "1 Transformation Step";
    } else if (pipelineSteps > 1) {
      return `${pipelineSteps} Transformation Steps`;
    }
  }

  render() {
    const pipeline = this.props.pipeline;

    if ([pipeline, pipeline.spec].includes(undefined)) {
      return <React.Fragment></React.Fragment>;
    }

    let editColumnAttribute;
    const transformSteps = pipeline.spec.transformationSteps;

    const transformStep =
      this.props.currentStep >= 0
        ? transformSteps[this.props.currentStep]
        : undefined;

    if (this.props.editColumn !== undefined) {
      editColumnAttribute = this.props.attributes.find(
        (attribute) => attribute === this.props.editColumn.attributeName
      );
    }

    return (
      <React.Fragment>
        {this.props.currentPage === "transform" && (
          <TransformationStepsList
            addTransformStepFunc={this.props.addTransformStepFunc}
            currentStep={this.props.currentStep}
            moveTransformStepFunc={this.props.moveTransformStepFunc}
            moveToStepFunc={this.props.moveToStepFunc}
            transformationSteps={transformSteps}
            removeTransformStepFunc={this.props.removeTransformStepFunc}
          />
        )}
        <Grid
          addColumnFunc={this.props.addColumnFunc}
          addedColumn={this.props.addedColumn}
          addTransformStepFunc={this.props.addTransformStepFunc}
          filters={this.props.pipeline.spec.filters}
          attributes={this.props.attributes}
          attributeProfiles={this.props.attributeProfiles}
          currentPage={this.props.currentPage}
          currentStep={this.props.currentStep}
          editColumnAttribute={editColumnAttribute}
          editColumnFunc={this.props.editColumnFunc}
          filters={this.props.filters}
          handleFilterChangeFunc={this.props.handleFilterChangeFunc}
          handleTransformStepChangeFunc={
            this.props.handleTransformStepChangeFunc
          }
          introducedAttributes={[]}
          moveTransformStepFunc={this.props.moveTransformStepFunc}
          moveToStepFunc={this.props.moveToStepFunc}
          originalRecordsSize={this.props.originalRecordsSize}
          pipeline={this.props.pipeline}
          profile={this.props.profile}
          removeColumnFunc={this.props.removeColumnFunc}
          removeTransformStepFunc={this.props.removeTransformStepFunc}
          sampleRecords={this.props.sampleRecords}
          transforms={this.props.transforms}
        />
        {this.props.contextBarActive &&
          ["filter", "transform"].includes(this.props.currentPage) && (
            <ContextSidebar>
              {this.props.editColumn !== undefined &&
                this.props.editColumn.type === "transform" && (
                  <EditTransformation
                    attribute={this.props.editColumn.attribute}
                    attributes={this.props.attributes}
                    currentStep={this.props.currentStep}
                    editColumn={this.props.editColumn}
                    filters={this.props.filters}
                    handleChangeFunc={this.props.handleTransformStepChangeFunc}
                    hideContextBarFunc={this.props.hideContextBarFunc}
                    previewState={this.props.previewState}
                    profile={this.props.profile}
                    transformStep={transformStep}
                    transforms={this.props.transforms}
                  />
                )}
              {this.props.editColumn !== undefined &&
                this.props.editColumn.type === "filter" && (
                  <EditFilter
                    attribute={editColumnAttribute}
                    attributes={this.props.attributes}
                    editColumn={this.props.editColumn}
                    filter={this.props.editColumn.filter}
                    filters={this.props.filters}
                    profile={this.props.profile}
                    handleChangeFunc={this.props.handleFilterChangeFunc}
                    hideContextBarFunc={this.props.hideContextBarFunc}
                  />
                )}
            </ContextSidebar>
          )}
      </React.Fragment>
    );
  }
}

export default PipelineDesigner;

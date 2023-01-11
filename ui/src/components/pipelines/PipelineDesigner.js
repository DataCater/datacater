import React, { Component } from "react";
import ContextSidebar from "./pipeline_designer/ContextSidebar";
import Edit from "./pipeline_designer/context_bar/Edit";
import PreviewSettings from "./pipeline_designer/context_bar/PreviewSettings";
import StepsList from "./pipeline_designer/grid/StepsList";
import Toolbar from "./pipeline_designer/grid/Toolbar";
import Grid from "./pipeline_designer/Grid";

class PipelineDesigner extends Component {
  constructor(props) {
    super(props);
    this.state = {
      draggingItem: undefined,
      draggingName: undefined,
      draggingShortName: undefined,
      draggingOverItem: undefined,
      showGrid: true,
    };
    this.onDragStart = this.onDragStart.bind(this);
    this.onDragEnd = this.onDragEnd.bind(this);
    this.onDragOver = this.onDragOver.bind(this);
    this.onDragLeave = this.onDragLeave.bind(this);
    this.onDrop = this.onDrop.bind(this);
    this.toggleShowGrid = this.toggleShowGrid.bind(this);
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
      this.props.moveStepFunc(
        this.state.draggingItem,
        this.state.draggingOverItem + 1
      );
      this.setState({
        draggingItem: undefined,
        draggingOverItem: undefined,
      });
    }
  }

  toggleShowGrid(event) {
    event.preventDefault();

    this.setState({
      showGrid: !this.state.showGrid,
    });
  }

  render() {
    const pipeline = this.props.pipeline;

    if ([pipeline, pipeline.spec].includes(undefined)) {
      return <React.Fragment></React.Fragment>;
    }

    let editColumnField;

    const steps = pipeline.spec.steps;
    const step =
      this.props.currentStep >= 0
        ? steps[this.props.currentStep - 1]
        : undefined;

    if (this.props.editColumn !== undefined) {
      editColumnField = this.props.fields.find(
        (field) => field === this.props.editColumn.fieldName
      );
    }

    return (
      <React.Fragment>
        <StepsList
          addStepFunc={this.props.addStepFunc}
          currentStep={this.props.currentStep}
          moveStepFunc={this.props.moveStepFunc}
          moveToStepFunc={this.props.moveToStepFunc}
          steps={steps}
          removeStepFunc={this.props.removeStepFunc}
        />
        <Toolbar
          currentStep={this.props.currentStep}
          hideStepNameFormFunc={this.props.hideStepNameFormFunc}
          pipeline={pipeline}
          editColumnFunc={this.props.editColumnFunc}
          inspectLimit={this.props.inspectLimit}
          filters={this.props.filters}
          removeStepFunc={this.props.removeStepFunc}
          sampleRecords={this.props.sampleRecords}
          showStepNameForm={this.props.showStepNameForm}
          showStepNameFormFunc={this.props.showStepNameFormFunc}
          step={step}
          streamInspectLength={this.props.streamInspectLength}
          toggleShowSettingsFunc={this.props.toggleShowSettingsFunc}
          transforms={this.props.transforms}
          updateStepNameFunc={this.props.updateStepNameFunc}
        />
        {this.state.showGrid && (
          <Grid
            addColumnFunc={this.props.addColumnFunc}
            addedColumn={this.props.addedColumn}
            addStepFunc={this.props.addStepFunc}
            filters={this.props.pipeline.spec.filters}
            fields={this.props.fields}
            fieldProfiles={this.props.fieldProfiles}
            currentStep={this.props.currentStep}
            editColumnField={editColumnField}
            editColumnFunc={this.props.editColumnFunc}
            filters={this.props.filters}
            handleFilterChangeFunc={this.props.handleFilterChangeFunc}
            handleStepChangeFunc={this.props.handleStepChangeFunc}
            introducedFields={[]}
            moveStepFunc={this.props.moveStepFunc}
            moveToStepFunc={this.props.moveToStepFunc}
            openDebugViewFunc={this.props.openDebugViewFunc}
            originalRecordsSize={this.props.originalRecordsSize}
            pipeline={this.props.pipeline}
            profile={this.props.profile}
            removeColumnFunc={this.props.removeColumnFunc}
            removeStepFunc={this.props.removeStepFunc}
            sampleRecords={this.props.sampleRecords}
            step={step}
            transforms={this.props.transforms}
          />
        )}
        {!this.state.showGrid && (
          <div className="container">
            <div className="row my-2">
              <div className="col-12">
                <div className="card">
                  <div className="card-body">
                    <pre>
                      {JSON.stringify(this.props.sampleRecords, null, 2)}
                    </pre>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
        {this.props.contextBarActive &&
          this.props.currentStep !== undefined &&
          this.props.editColumn !== undefined &&
          !this.props.showSettings && (
            <ContextSidebar>
              <Edit
                field={this.props.editColumn.field}
                fields={this.props.fields}
                currentStep={this.props.currentStep}
                editColumn={this.props.editColumn}
                filters={this.props.filters}
                handleChangeFunc={this.props.handleStepChangeFunc}
                hideContextBarFunc={this.props.hideContextBarFunc}
                previewState={this.props.previewState}
                transformStep={step}
                profile={this.props.profile}
                transforms={this.props.transforms}
              />
            </ContextSidebar>
          )}
        {this.props.contextBarActive && this.props.showSettings && (
          <ContextSidebar>
            <PreviewSettings
              hideContextBarFunc={this.props.hideContextBarFunc}
              inspectLimit={this.props.inspectLimit}
              pipeline={this.props.pipeline}
              showGrid={this.state.showGrid}
              toggleShowGridFunc={this.toggleShowGrid}
              updateInspectLimitFunc={this.props.updateInspectLimitFunc}
            />
          </ContextSidebar>
        )}
      </React.Fragment>
    );
  }
}

export default PipelineDesigner;

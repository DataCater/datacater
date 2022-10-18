import React, { Component } from "react";
import { connect } from "react-redux";
import {
  AlertTriangle,
  RefreshCcw,
  PlayCircle,
  Check,
  Code,
  Copy,
  Filter,
  Hash,
  HelpCircle,
  List,
  Search,
  Table,
  Type,
} from "react-feather";
import { Modal } from "react-bootstrap";
import BaseTable, { AutoResizer } from "react-base-table";
import PipelineDesigner from "../../components/pipelines/PipelineDesigner";
import Nav from "../../components/pipelines/pipeline_designer/Nav";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";
import { deepCopy } from "../../helpers/deepCopy";
import { profileRecords } from "../../helpers/profileRecords";
import {
  fetchPipeline,
  updatePipeline,
  inspectPipeline,
} from "../../actions/pipelines";
import { inspectStream } from "../../actions/streams";
import { fetchFilters } from "../../actions/filters";
import { fetchTransforms } from "../../actions/transforms";
import "../../scss/grid.scss";
import "../../scss/grid/statistics.scss";

class EditPipeline extends Component {
  constructor(props) {
    super(props);
    this.state = {
      // storing, whether a new column has been added, in the state
      // is a bit nasty but will speed up the componentDidMount function
      addedColumn: false,
      editColumn: undefined,
      contextBarActive: false,
      currentPage: "explore",
      currentStep: undefined,
      errorMessage: "",
      pipeline: {},
      pipelineUpdated: false,
      pipelineUpdatedAt: undefined,
      unpersistedChanges: false,
    };

    this.monitorChanges = this.monitorChanges.bind(this);
    this.updateSampleRecords = this.updateSampleRecords.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.editColumn = this.editColumn.bind(this);
    this.handleFilterChange = this.handleFilterChange.bind(this);
    this.handleTransformStepChange = this.handleTransformStepChange.bind(this);
    this.delayedReloadSampleRecords =
      this.delayedReloadSampleRecords.bind(this);
    this.addTransformStep = this.addTransformStep.bind(this);
    this.removeTransformStep = this.removeTransformStep.bind(this);
    this.moveTransformStep = this.moveTransformStep.bind(this);
    this.moveToStep = this.moveToStep.bind(this);
    this.moveToPage = this.moveToPage.bind(this);
    this.addColumn = this.addColumn.bind(this);
    this.showContextBar = this.showContextBar.bind(this);
    this.hideContextBar = this.hideContextBar.bind(this);
    this.removeColumn = this.removeColumn.bind(this);
  }

  componentDidMount() {
    this.props.fetchPipeline(this.getPipelineId()).then(() => {
      const pipeline = this.props.pipelines.pipeline;

      // Load sample records from the source stream
      if (pipeline.metadata["stream-in"] !== undefined) {
        this.props.inspectStream(pipeline.metadata["stream-in"]);
      }

      this.setState({
        pipeline: pipeline,
      });
    });

    this.props.fetchFilters();
    this.props.fetchTransforms();

    // initialize monitoring of pipeline changes
    if (this.monitorChangesIntervalId != null) {
      clearInterval(this.monitorChangesIntervalId);
    }

    this.monitorChangesIntervalId = setInterval(this.monitorChanges, 5000);
  }

  componentWillUnmount() {
    clearInterval(this.monitorChangesIntervalId);
    this.monitorChanges();
  }

  getPipelineId() {
    return this.props.match.params.id;
  }

  monitorChanges() {
    if (this.state.unpersistedChanges) {
      return this.props
        .updatePipeline(this.getPipelineId(), this.state.pipeline)
        .then(() => {
          this.setState({
            unpersistedChanges: false,
          });
        });
    } else {
      return Promise.resolve(undefined);
    }
  }

  updateSampleRecords(pipeline, previewStage, previewStep) {
    const domElement = document.querySelector(".datacater-grid-container");
    if (domElement != null) {
      domElement.style.opacity = 0.2;
    }

    clearTimeout(this.updateSampleRecordsTimeout);
    this.updateSampleRecordsTimeout = setTimeout(() => {
      let updatedSampleRecords = undefined;
      this.props
        .inspectPipeline(
          this.state.pipeline,
          this.props.streams.inspectionResult,
          previewStage,
          previewStep
        )
        .then(() => {
          if (domElement != null) {
            domElement.style.opacity = 1;
          }
          if (
            document.querySelector(".loading-pipeline-designer-wrapper") != null
          ) {
            document
              .querySelector(".loading-pipeline-designer-wrapper")
              .classList.remove("d-block");
          }
        });
    }, 50);
  }

  handleChange(event) {
    let pipeline = deepCopy(this.state.pipeline);
    pipeline[event.target.name] = event.target.value;
    this.setState({
      addedColumn: false,
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  handleFilterChange(event, fieldName, property, value) {
    let pipeline = deepCopy(this.state.pipeline);
    let editColumn = this.state.editColumn;
    let contextBarActive = true;
    let type = event.target.type;

    if (fieldName === undefined && property === undefined) {
      fieldName = event.target.dataset.fieldname;
      property = event.target.name;
      value = event.target.value;
      type = "text";
    }

    // Delete existing filter on given field, if it exists
    const filterIdx = pipeline.spec.filters.findIndex(
      (filter) => filter.fieldName === fieldName
    );
    if (filterIdx > -1) {
      if (property === "filter") {
        if (value === undefined) {
          // Remove filter
          pipeline.spec.filters.splice(filterIdx, 1);
          editColumn = undefined;
          contextBarActive = false;
        } else {
          pipeline.spec.filters[filterIdx].filter = value;
          editColumn.filter = pipeline.spec.filters[filterIdx];
        }
      } else {
        pipeline.spec.filters[filterIdx].filterConfig[property] = value;
        editColumn.filter = pipeline.spec.filters[filterIdx];
      }
    } else {
      const newFilter = {
        fieldName: fieldName,
        filter: value,
        filterConfig: {},
      };
      pipeline.spec.filters.push(newFilter);
      editColumn.filter = newFilter;
    }

    this.delayedReloadSampleRecords(pipeline, type, "filter");

    this.setState({
      contextBarActive: contextBarActive,
      editColumn: editColumn,
      addedColumn: false,
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  createEditColumnFilterObject(fieldName) {
    const pipeline = deepCopy(this.state.pipeline);
    const filter = pipeline.spec.filters.find(
      (filter) => filter.fieldName === fieldName
    );

    return {
      type: "filter",
      fieldName: fieldName,
      filter: Object.assign({}, filter),
    };
  }

  createEditColumnTransformationObject(fieldName, stepIndex) {
    const pipeline = deepCopy(this.state.pipeline);

    const field = pipeline.spec.transformationSteps[
      stepIndex
    ].transformations.find(
      (transform) => transform.fieldName === fieldName
    );

    return {
      fieldName: fieldName,
      stepIndex: stepIndex,
      type: "transform",
    };
  }

  handleTransformStepChange(
    event,
    currentStep,
    fieldName,
    property,
    value,
    prefix
  ) {
    let pipeline = deepCopy(this.state.pipeline);
    let editColumn = this.state.editColumn;
    let contextBarActive = true;

    if (
      currentStep === undefined &&
      fieldName === undefined &&
      property === undefined
    ) {
      fieldName = event.target.dataset.fieldname;
      currentStep = event.target.dataset.currentstep;
      prefix = event.target.dataset.prefix;
      property = event.target.name;
      value = event.target.value;
    }

    const transformIdx = pipeline.spec.transformationSteps[
      currentStep
    ].transformations.findIndex(
      (transform) => transform.fieldName === fieldName
    );

    if (transformIdx > -1) {
      if (property === "transform") {
        if (value === undefined) {
          // Remove transform
          pipeline.spec.transformationSteps[currentStep].transformations.splice(
            transformIdx,
            1
          );
          editColumn = undefined;
          contextBarActive = false;
        } else {
          pipeline.spec.transformationSteps[currentStep].transformations[
            transformIdx
          ].transformation = value;
          editColumn.transform =
            pipeline.spec.transformationSteps[currentStep].transformations[
              transformIdx
            ];
        }
      } else if (property === "filter") {
        pipeline.spec.transformationSteps[currentStep].transformations[
          transformIdx
        ].filter = value;
        editColumn.transform =
          pipeline.spec.transformationSteps[currentStep].transformations[
            transformIdx
          ];
      } else {
        pipeline.spec.transformationSteps[currentStep].transformations[
          transformIdx
        ][prefix][property] = value;
        editColumn.transform =
          pipeline.spec.transformationSteps[currentStep].transformations[
            transformIdx
          ];
      }
    } else {
      const newTransform = {
        fieldName: fieldName,
        transformation: value,
        transformationConfig: {},
        filter: null,
        filterConfig: {},
      };
      pipeline.spec.transformationSteps[currentStep].transformations.push(
        newTransform
      );
      editColumn.transform = newTransform;
    }

    const type = event !== undefined ? event.target.type : "text";

    this.delayedReloadSampleRecords(pipeline, type, "transform", currentStep);

    this.setState({
      addedColumn: false,
      contextBarActive: contextBarActive,
      editColumn: editColumn,
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  delayedReloadSampleRecords(pipeline, eventType, previewStage, previewStep) {
    if (eventType === "text") {
      let domElements = [
        document.querySelector(".datacater-grid-container"),
      ].filter((_) => _ != null);
      domElements.forEach(function (el) {
        el.style.opacity = 0.2;
      });
      clearTimeout(this.bounceTimeout);
      this.bounceTimeout = setTimeout(() => {
        // opacity will be reset by the following function call
        this.updateSampleRecords(pipeline, previewStage, previewStep);
      }, 500);
    } else {
      this.updateSampleRecords(pipeline, previewStage, previewStep);
    }
  }

  addTransformStep(event, useCache) {
    event.preventDefault();

    let pipeline = deepCopy(this.state.pipeline);

    pipeline.spec.transformationSteps.push({
      name: "",
      transformations: [],
    });

    const currentStep = pipeline.spec.transformationSteps.length - 1;

    this.updateSampleRecords(pipeline, "transform", currentStep);

    this.setState({
      addedColumn: false,
      contextBarActive: false,
      currentPage: "transform",
      currentStep: currentStep,
      editColumn: undefined,
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  removeTransformStep(stepIdx) {
    let pipeline = deepCopy(this.state.pipeline);
    let currentPage = this.state.currentPage;
    let currentStep = this.state.currentStep;

    // Remove stepIdx from pipeline.spec.transformationSteps
    pipeline.spec.transformationSteps.splice(stepIdx, 1);

    if (currentStep > pipeline.spec.transformationSteps.length - 1) {
      if (pipeline.spec.transformationSteps.length === 0) {
        currentPage = "explore";
      } else {
        currentStep = pipeline.spec.transformationSteps.length - 1;
      }
    }

    this.updateSampleRecords(pipeline, "transform", currentStep);

    this.setState({
      addedColumn: false,
      currentPage: currentPage,
      currentStep: currentStep,
      contextBarActive: false,
      editColumn: undefined,
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  moveTransformStep(fromPosition, toPosition) {
    let pipeline = deepCopy(this.state.pipeline);
    let transformSteps = pipeline.step.transformationSteps;
    const movingStepIndex = transformSteps.findIndex(function (el) {
      return el.sortPosition === fromPosition;
    });

    // TODO
    if (fromPosition > toPosition) {
      pipelineSteps = pipelineSteps.map(function (step) {
        if (
          step.sortPosition >= toPosition &&
          step.sortPosition < fromPosition
        ) {
          step.sortPosition++;
        }
        return step;
      });
      pipelineSteps[movingStepIndex].sortPosition = toPosition;
    } else if (fromPosition < toPosition) {
      pipelineSteps = pipelineSteps.map(function (step) {
        if (
          step.sortPosition > fromPosition &&
          step.sortPosition < toPosition
        ) {
          step.sortPosition--;
        }
        return step;
      });
      pipelineSteps[movingStepIndex].sortPosition = toPosition - 1;
    }

    pipeline.spec.transformationSteps = transformSteps;

    const newCurrentStep = toPosition;

    this.updateSampleRecords(pipeline, "transform", newCurrentStep);

    this.setState({
      addedColumn: false,
      currentStep: newCurrentStep,
      contextBarActive: false,
      editColumn: undefined,
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  moveToStep(event, newStep) {
    event.preventDefault();
    this.updateSampleRecords(this.state.pipeline, "transform", newStep);
    this.setState({
      addedColumn: false,
      codeView: false,
      currentPage: "transform",
      currentStep: newStep,
      contextBarActive: false,
      editColumn: undefined,
    });
  }

  moveToPage(newPage, newStep) {
    window.scrollTo(0, 0);

    const pipelineId = this.getPipelineId();

    this.updateSampleRecords(this.state.pipeline, newPage, newStep);

    if (
      newPage === "transform" &&
      this.state.pipeline.spec.transformationSteps.length === 0
    ) {
      const pipeline = this.state.pipeline;
      pipeline.spec.transformationSteps.push({
        name: "",
        transformations: [],
      });
      this.setState({
        addedColumn: false,
        currentPage: newPage,
        currentStep: 0,
        contextBarActive: false,
        editColumn: undefined,
        pipeline: pipeline,
        unpersistedChanges: true,
      });
    } else {
      this.setState({
        addedColumn: false,
        currentPage: newPage,
        currentStep: newStep,
        contextBarActive: false,
        editColumn: undefined,
      });
    }
  }

  addColumn(event) {
    event.preventDefault();
    let pipeline = deepCopy(this.state.pipeline);
    const stepIndex = pipeline.pipelineSteps.findIndex(
      (_) => _.sortPosition === this.state.currentStep
    );
    if (stepIndex >= 0) {
      // add new virtual field to data source profile
      const that = this;
      this.props
        .addDataSourceProfileField(
          this.props.dataSourceProfiles.dataSourceProfile.id
        )
        .then(function () {
          const virtualField =
            that.props.dataSourceProfiles.dataSourceProfileField;
          if (virtualField != null) {
            pipeline.pipelineSteps[stepIndex].fields.push({
              transformFieldId: virtualField.id,
              transformationAction: "add-column",
              transformationFilter: "",
              actionValue: "",
              filterValue: "string",
              isKey: false,
            });
            // Add column to all successive pipeline steps
            pipeline.pipelineSteps = pipeline.pipelineSteps.map(function (
              pipelineStep
            ) {
              if (pipelineStep.sortPosition > that.state.currentStep) {
                pipelineStep.fields.push({
                  transformFieldId: virtualField.id,
                  transformationAction: "",
                  transformationFilter: "",
                  actionValue: "",
                  filterValue: "",
                  isKey: false,
                });
              }
              return pipelineStep;
            });

            that.updateSampleRecords(
              pipeline,
              "transform",
              that.state.currentStep
            );

            that.setState({
              addedColumn: true,
              contextBarActive: false,
              editColumn: undefined,
              pipeline: pipeline,
              unpersistedChanges: true,
            });
          }
        });
    }
  }

  editColumn(fieldName, sortPosition, type) {
    const pipeline = deepCopy(this.state.pipeline);
    let editColumn = undefined;

    if (
      this.state.editColumn !== undefined &&
      fieldName === this.state.editColumn.fieldName
    ) {
      // hide context bar if we click the edit button of an active field again
      this.hideContextBar();
    } else {
      // show context bar when editing a transformation or filter
      this.showContextBar();

      if (
        type === "transform" &&
        fieldName !== undefined &&
        sortPosition !== undefined
      ) {
        editColumn = this.createEditColumnTransformationObject(
          fieldName,
          sortPosition
        );
      } else if (type === "filter" && fieldName !== undefined) {
        editColumn = this.createEditColumnFilterObject(fieldName);
      }
    }

    this.setState({
      editColumn: editColumn,
    });
  }

  showContextBar() {
    this.setState({
      contextBarActive: true,
    });
  }

  hideContextBar() {
    this.setState({
      contextBarActive: false,
      editColumn: undefined,
    });
  }

  removeColumn(event) {
    event.preventDefault();

    const fieldId = parseInt(event.target.dataset.fieldId);

    // remove column from all pipeline steps
    let pipeline = deepCopy(this.state.pipeline);
    pipeline.pipelineSteps = pipeline.pipelineSteps.map(function (step) {
      step.fields = step.fields.filter(
        (_) => _.transformFieldId !== fieldId
      );
      return step;
    });

    this.updateSampleRecords(pipeline, "transform", this.state.currentStep);

    this.setState({
      addedColumn: false,
      contextBarActive: false,
      editColumn: undefined,
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  render() {
    const pipeline = this.state.pipeline;

    const header = (
      <div className="row">
        <Breadcrumb
          items={[
            { name: "Pipelines", uri: "/pipelines" },
            { name: pipeline.uuid, uri: `/pipelines/${pipeline.uuid}` },
            { name: "Edit" },
          ]}
        />
        <div className="col-12 mt-3">
          <div
            className="card welcome-card py-2"
            style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
          >
            <div className="card-body text-center p-0">
              <div className="row justify-content-center">
                <div className="col-8 text-start d-flex align-items-center overflow-hidden text-nowrap">
                  <h4 className="fw-semibold mb-0">
                    {pipeline.name || "Untitled pipeline"}
                  </h4>
                </div>
                <div className="col-4 d-flex align-items-center justify-content-end">
                  <div>
                    <button
                      className="btn btn-sm btn-light btn-pill"
                      onClick={(e) => e.preventDefault()}
                    >
                      {this.props.pipelines.updatingPipeline ||
                      this.state.unpersistedChanges
                        ? "Syncing..."
                        : "Synced"}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );

    const loader = (
      <div className="row">
        <div className="col-12 justify-content-center d-flex pt-5 mt-5">
          <button
            className="btn btn-primary d-flex align-items-center"
            type="button"
            disabled
          >
            <span
              className="spinner-border spinner-border-sm me-2"
              role="status"
              aria-hidden="true"
            ></span>
            Loading sample records...
          </button>
        </div>
      </div>
    );

    if (
      pipeline !== undefined &&
      pipeline.metadata !== undefined &&
      pipeline.metadata["stream-in"] === undefined
    ) {
      return (
        <div className="container">
          {header}
          <div className="alert alert-warning mt-4" role="alert">
            Please go to{" "}
            <a
              className="text-black"
              href={`/pipelines/${pipeline.uuid}/settings`}
            >
              Settings
            </a>{" "}
            and assign a source stream.
          </div>
        </div>
      );
    }

    if (
      pipeline === undefined ||
      this.props.streams.inspectingStream ||
      this.props.streams.inspectionResult === undefined
    ) {
      return (
        <div className="container">
          {header}
          {loader}
        </div>
      );
    }

    const sampleRecords =
      this.state.currentPage === "explore" ||
      this.props.pipelines.inspectionResult === undefined
        ? deepCopy(this.props.streams.inspectionResult).map(
            (record) => record.value
          )
        : deepCopy(this.props.pipelines.inspectionResult).map(
            (record) => record.value
          );

    if (sampleRecords.length === 0) {
      return (
        <div className="container">
          {header}
          <div className="alert alert-warning mt-4" role="alert">
            We couldn&apos;t extract any sample records. Is your source stream
            empty?
          </div>
        </div>
      );
    }

    const profile = profileRecords(sampleRecords);

    const records = sampleRecords.map(function (sample, index) {
      return Object.assign({}, sample, { id: index + 1 });
    });

    let classNames = "create-pipeline-form pipeline-designer";

    if (this.state.currentPage !== "steps") {
      classNames += " pipeline-designer-page";
    }

    if (this.state.contextBarActive && this.state.currentStep !== undefined) {
      classNames += " datacater-context-bar-active";
    }

    if (this.state.currentPage === "steps" && this.state.currentStep > 0) {
      classNames += " pipeline-designer-transformations";
    }

    return (
      <>
        <div className="container">
          {header}
          <div className="row g-3 align-items-center mt-0">
            <div className="col-6 mb-2">
              <Nav
                currentPage={this.state.currentPage}
                moveToPageFunc={this.moveToPage}
              />
            </div>
          </div>
        </div>
        {sampleRecords.length > 0 && (
          <PipelineDesigner
            addTransformStepFunc={this.addTransformStep}
            fields={Object.keys(profile)}
            contextBarActive={this.state.contextBarActive}
            currentPage={this.state.currentPage}
            currentStep={this.state.currentStep}
            editColumn={this.state.editColumn}
            editColumnFunc={this.editColumn}
            filters={this.props.filters.filters}
            handleFilterChangeFunc={this.handleFilterChange}
            handleTransformStepChangeFunc={this.handleTransformStepChange}
            hideContextBarFunc={this.hideContextBar}
            moveToStepFunc={this.moveToStep}
            moveTransformStepFunc={this.moveTransformStep}
            pipeline={pipeline}
            previewState={{}}
            profile={profile}
            removeTransformStepFunc={this.removeTransformStep}
            sampleRecords={sampleRecords}
            transforms={this.props.transforms.transforms}
          />
        )}
      </>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    filters: state.filters,
    pipelines: state.pipelines,
    streams: state.streams,
    transforms: state.transforms,
  };
};

const mapDispatchToProps = {
  fetchFilters: fetchFilters,
  fetchPipeline: fetchPipeline,
  fetchTransforms: fetchTransforms,
  inspectPipeline: inspectPipeline,
  inspectStream: inspectStream,
  updatePipeline: updatePipeline,
};

export default connect(mapStateToProps, mapDispatchToProps)(EditPipeline);

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
  X,
} from "react-feather";
import { Modal } from "react-bootstrap";
import BaseTable, { AutoResizer } from "react-base-table";
import PipelineDesigner from "../../components/pipelines/PipelineDesigner";
import DebugView from "../../components/pipelines/pipeline_designer/grid/DebugView";
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
      currentStep: undefined,
      debugRecord: undefined,
      errorMessage: "",
      pipeline: {},
      pipelineUpdated: false,
      pipelineUpdatedAt: undefined,
      showStepNameForm: false,
      unpersistedChanges: false,
    };

    this.monitorChanges = this.monitorChanges.bind(this);
    this.updateSampleRecords = this.updateSampleRecords.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.editColumn = this.editColumn.bind(this);
    this.handleFilterChange = this.handleFilterChange.bind(this);
    this.handleStepChange = this.handleStepChange.bind(this);
    this.delayedReloadSampleRecords =
      this.delayedReloadSampleRecords.bind(this);
    this.addStep = this.addStep.bind(this);
    this.removeStep = this.removeStep.bind(this);
    this.moveStep = this.moveStep.bind(this);
    this.moveToStep = this.moveToStep.bind(this);
    this.addColumn = this.addColumn.bind(this);
    this.showContextBar = this.showContextBar.bind(this);
    this.hideContextBar = this.hideContextBar.bind(this);
    this.removeColumn = this.removeColumn.bind(this);
    this.updateStepName = this.updateStepName.bind(this);
    this.showStepNameForm = this.showStepNameForm.bind(this);
    this.hideStepNameForm = this.hideStepNameForm.bind(this);
    this.openDebugView = this.openDebugView.bind(this);
    this.closeDebugView = this.closeDebugView.bind(this);
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

  updateSampleRecords(pipeline, previewStep) {
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
          // UI starts at index 1, Python runner starts at index 0
          previewStep - 1
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

  createEditRecordObject(stepIndex, step) {
    return {
      stepIndex: stepIndex,
      step: step,
    };
  }

  createEditColumnObject(fieldName, stepIndex) {
    const pipeline = deepCopy(this.state.pipeline);

    const step = pipeline.spec.steps[stepIndex];

    return {
      fieldName: fieldName,
      stepIndex: stepIndex,
      step: step,
    };
  }

  handleStepChange(event, currentStep, fieldName, property, value, prefix) {
    let pipeline = deepCopy(this.state.pipeline);
    let editColumn = this.state.editColumn;
    let contextBarActive = true;

    if (currentStep === undefined && event !== undefined) {
      currentStep = event.target.dataset.currentstep;
    }
    if (fieldName === undefined && event !== undefined) {
      fieldName = event.target.dataset.fieldname;
    }
    if (prefix === undefined && event !== undefined) {
      prefix = event.target.dataset.prefix;
    }
    if (property === undefined && event !== undefined) {
      property = event.target.name;
    }
    if (value === undefined && event !== undefined) {
      value = event.target.value;
    }

    const step = pipeline.spec.steps[currentStep - 1];

    if (step.kind === "Record") {
      if (property === "transform") {
        if ([undefined, ""].includes(value)) {
          // Remove transform
          delete step.transform;
          pipeline.spec.steps[currentStep - 1] = step;
          editColumn = undefined;
        } else {
          pipeline.spec.steps[currentStep - 1]["transform"] = {
            key: value,
          };
        }
      } else if (property === "filter") {
        if ([undefined, ""].includes(value)) {
          // Remove filter
          delete step.filter;
          pipeline.spec.steps[currentStep - 1] = step;
          editColumn = undefined;
        } else {
          pipeline.spec.steps[currentStep - 1]["filter"] = {
            key: value,
          };
        }
      } else {
        const config = {};
        config[property] = value;
        pipeline.spec.steps[currentStep - 1][prefix]["config"] = Object.assign(
          {},
          pipeline.spec.steps[currentStep - 1][prefix]["config"],
          config
        );
      }
    } else {
      if (value !== undefined) {
        pipeline.spec.steps[currentStep - 1].fields[fieldName] = Object.assign(
          {},
          pipeline.spec.steps[currentStep - 1].fields[fieldName]
        );
      }
      if (property === "transform") {
        if ([undefined, ""].includes(value)) {
          // Remove transform
          delete step.fields[fieldName].transform;
          // Remove field if no filter is defined
          if (Object.keys(step.fields[fieldName]).length === 0) {
            delete step.fields[fieldName];
          }
          pipeline.spec.steps[currentStep - 1] = step;
          editColumn = undefined;
        } else {
          pipeline.spec.steps[currentStep - 1].fields[fieldName]["transform"] =
            {
              key: value,
            };
        }
      } else if (property === "filter") {
        if ([undefined, ""].includes(value)) {
          // Remove filter
          delete step.fields[fieldName].filter;
          // Remove field if no transform is defined
          if (Object.keys(step.fields[fieldName]).length === 0) {
            delete step.fields[fieldName];
          }
          pipeline.spec.steps[currentStep - 1] = step;
          editColumn = undefined;
        } else {
          pipeline.spec.steps[currentStep - 1].fields[fieldName]["filter"] = {
            key: value,
          };
        }
      } else {
        const config = {};
        config[property] = value;
        pipeline.spec.steps[currentStep - 1].fields[fieldName][prefix][
          "config"
        ] = Object.assign(
          {},
          pipeline.spec.steps[currentStep - 1].fields[fieldName][prefix][
            "config"
          ],
          config
        );
      }
    }

    const type = event !== undefined ? event.target.type : "text";

    this.delayedReloadSampleRecords(pipeline, type, currentStep);

    this.setState({
      addedColumn: false,
      contextBarActive: contextBarActive,
      editColumn: editColumn,
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  delayedReloadSampleRecords(pipeline, eventType, previewStep) {
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
        this.updateSampleRecords(pipeline, previewStep);
      }, 500);
    } else {
      this.updateSampleRecords(pipeline, previewStep);
    }
  }

  addStep(event) {
    event.preventDefault();

    let pipeline = deepCopy(this.state.pipeline);
    const kind = event.target.dataset.stepKind;

    if (kind === "Record") {
      pipeline.spec.steps.push({
        kind: "Record",
      });
    } else {
      pipeline.spec.steps.push({
        kind: "Field",
        fields: {},
      });
    }

    const currentStep = pipeline.spec.steps.length;

    this.updateSampleRecords(pipeline, currentStep);

    this.setState({
      addedColumn: false,
      contextBarActive: false,
      currentStep: currentStep,
      editColumn: undefined,
      pipeline: pipeline,
      showStepNameForm: false,
      unpersistedChanges: true,
    });
  }

  removeStep(stepIdx) {
    let pipeline = deepCopy(this.state.pipeline);
    let currentStep = this.state.currentStep;

    // Remove stepIdx from pipeline.spec.steps
    pipeline.spec.steps.splice(stepIdx - 1, 1);

    if (currentStep > pipeline.spec.steps.length - 1) {
      if (pipeline.spec.steps.length === 0) {
        currentStep = undefined;
      } else {
        currentStep = pipeline.spec.steps.length - 1;
      }
    }

    // Step cannot go below 1
    if (currentStep < 1) {
      currentStep = 1;
    }

    this.updateSampleRecords(pipeline, currentStep);

    this.setState({
      addedColumn: false,
      currentStep: currentStep,
      contextBarActive: false,
      editColumn: undefined,
      pipeline: pipeline,
      showStepNameForm: false,
      unpersistedChanges: true,
    });
  }

  moveStep(fromPosition, toPosition) {
    if (!isNaN(toPosition)) {
      let pipeline = deepCopy(this.state.pipeline);
      let steps = pipeline.spec.steps;

      const movingStep = deepCopy(steps[fromPosition - 1]);

      if (fromPosition > toPosition) {
        for (let i = fromPosition - 1; i >= toPosition; i--) {
          steps[i] = steps[i - 1];
        }
        steps[toPosition - 1] = movingStep;
      } else if (fromPosition < toPosition) {
        toPosition = toPosition - 1;
        for (let i = fromPosition - 1; i < toPosition - 1; i++) {
          steps[i] = steps[i + 1];
        }
        steps[toPosition - 1] = movingStep;
      }

      pipeline.spec.steps = steps;

      const newCurrentStep = toPosition;

      this.updateSampleRecords(pipeline, newCurrentStep);

      this.setState({
        addedColumn: false,
        currentStep: newCurrentStep,
        contextBarActive: false,
        editColumn: undefined,
        pipeline: pipeline,
        showStepNameForm: false,
        unpersistedChanges: true,
      });
    }
  }

  moveToStep(event, newStep) {
    event.preventDefault();

    // newStep is undefined, if we switch to "Inspect source"
    // In this case, we do not need to evaluate the pipeline spec
    // against the source stream
    if (newStep !== undefined) {
      this.updateSampleRecords(this.state.pipeline, newStep);
    }

    this.setState({
      addedColumn: false,
      codeView: false,
      currentStep: newStep,
      contextBarActive: false,
      editColumn: undefined,
      showStepNameForm: false,
    });
  }

  // TODO
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

  editColumn(fieldName, sortPosition) {
    const pipeline = deepCopy(this.state.pipeline);
    let editColumn = undefined;

    const currentStep = pipeline.spec.steps[this.state.currentStep - 1];

    if (currentStep.kind === "Record") {
      if (this.state.editColumn !== undefined) {
        this.hideContextBar();
      } else {
        this.showContextBar();
        editColumn = this.createEditRecordObject(
          this.state.currentStep - 1,
          currentStep
        );
      }
    } else {
      if (
        this.state.editColumn !== undefined &&
        fieldName === this.state.editColumn.fieldName
      ) {
        // hide context bar if we click the edit button of an active field again
        this.hideContextBar();
      } else {
        // show context bar when editing a transformation or filter
        this.showContextBar();

        if (fieldName !== undefined && sortPosition !== undefined) {
          editColumn = this.createEditColumnObject(fieldName, sortPosition);
        }
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

  // TODO
  removeColumn(event) {
    event.preventDefault();

    const fieldId = parseInt(event.target.dataset.fieldId);

    // remove column from all pipeline steps
    let pipeline = deepCopy(this.state.pipeline);
    pipeline.pipelineSteps = pipeline.pipelineSteps.map(function (step) {
      step.fields = step.fields.filter((_) => _.transformFieldId !== fieldId);
      return step;
    });

    this.updateSampleRecords(pipeline, "transform", this.state.currentStep);

    this.setState({
      addedColumn: false,
      contextBarActive: false,
      editColumn: undefined,
      pipeline: pipeline,
      showStepNameForm: false,
      unpersistedChanges: true,
    });
  }

  updateStepName(event) {
    let pipeline = this.state.pipeline;

    pipeline.spec.steps[this.state.currentStep - 1]["name"] =
      event.target.value;

    this.setState({
      pipeline: pipeline,
      unpersistedChanges: true,
    });
  }

  showStepNameForm() {
    this.setState({
      showStepNameForm: true,
    });
  }

  hideStepNameForm() {
    this.setState({
      showStepNameForm: false,
    });
  }
  openDebugView(record) {
    this.setState({
      debugRecord: record,
    });
  }

  closeDebugView() {
    this.setState({
      debugRecord: undefined,
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
                    {this.props.pipelines.inspectingPipelineFailed ===
                      false && (
                      <button
                        className="btn btn-sm btn-primary text-white btn-pill me-2"
                        onClick={(e) => e.preventDefault()}
                      >
                        <span className="d-flex align-items-center">
                          <Check className="feather-icon" />
                          Preview
                        </span>
                      </button>
                    )}
                    {this.props.pipelines.inspectingPipelineFailed === true && (
                      <button
                        className="btn btn-sm btn-danger btn-pill me-2"
                        onClick={(e) => {
                          e.preventDefault();
                          this.updateSampleRecords(
                            this.state.pipeline,
                            this.state.currentStep
                          );
                        }}
                      >
                        <span className="d-flex align-items-center">
                          <X className="feather-icon" />
                          Preview failed (Retry)
                        </span>
                      </button>
                    )}
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
      this.props.streams.errorMessage !== undefined ||
      this.props.pipelines.errorMessage !== undefined
    ) {
      return (
        <div className="container">
          {header}
          <div className="alert alert-danger mt-4" role="alert">
            <p className="h6 fs-bolder">API response:</p>
            {this.props.streams.errorMessage ||
              this.props.pipelines.errorMessage}
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
      this.state.currentStep === undefined ||
      this.props.pipelines.inspectionResult === undefined
        ? deepCopy(this.props.streams.inspectionResult)
        : deepCopy(this.props.pipelines.inspectionResult);

    if (this.state.currentStep === undefined && sampleRecords.length === 0) {
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

    let classNames = "";

    if (this.state.contextBarActive && this.state.currentStep !== undefined) {
      classNames += "datacater-context-bar-active";
    }

    return (
      <div className={classNames}>
        <div className="container">{header}</div>
        {this.state.debugRecord !== undefined && (
          <DebugView
            closeDebugViewFunc={this.closeDebugView}
            pipeline={this.state.pipeline}
            record={this.state.debugRecord}
          />
        )}
        {sampleRecords !== undefined && (
          <PipelineDesigner
            addStepFunc={this.addStep}
            fields={Object.keys(profile)}
            contextBarActive={this.state.contextBarActive}
            currentStep={this.state.currentStep}
            editColumn={this.state.editColumn}
            editColumnFunc={this.editColumn}
            filters={this.props.filters.filters}
            handleFilterChangeFunc={this.handleFilterChange}
            handleStepChangeFunc={this.handleStepChange}
            hideContextBarFunc={this.hideContextBar}
            hideStepNameFormFunc={this.hideStepNameForm}
            moveToStepFunc={this.moveToStep}
            moveStepFunc={this.moveStep}
            openDebugViewFunc={this.openDebugView}
            pipeline={pipeline}
            previewState={{}}
            profile={profile}
            removeStepFunc={this.removeStep}
            sampleRecords={sampleRecords}
            showStepNameForm={this.state.showStepNameForm}
            showStepNameFormFunc={this.showStepNameForm}
            transforms={this.props.transforms.transforms}
            updateStepNameFunc={this.updateStepName}
          />
        )}
      </div>
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

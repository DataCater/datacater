import React, { Component } from "react";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import { Filter as FilterIcon, HelpCircle } from "react-feather";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import CodeEditor from "./CodeEditor";
import { jsonToYaml } from "../../../../helpers/jsonToYaml";

class FilterConfig extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentTab: "config",
    };

    this.selectTab = this.selectTab.bind(this);
  }

  selectTab(event) {
    event.preventDefault();

    this.setState({
      currentTab: event.target.dataset.tab,
    });
  }

  render() {
    const {
      field,
      fields,
      fieldDataType,
      currentStep,
      editColumn,
      filter,
      filters,
      flattenedDataType,
      handleChangeFunc,
      pipelineField,
      pipelineStep,
      sortPosition,
      transform,
      transformStep,
    } = this.props;

    let filterConfig = {};
    let filterExpectsDataType = true;

    if (transformStep.kind === "Record") {
      // Load config of the record-level filter
      if (
        transformStep.filter !== undefined &&
        transformStep.filter.config !== undefined
      ) {
        filterConfig = transformStep.filter.config;
      }
    } else {
      // Load config of the field-level filter
      if (
        pipelineField !== undefined &&
        pipelineField.filter !== undefined &&
        pipelineField.filter.config !== undefined
      ) {
        filterConfig = pipelineField.filter.config;
      }

      // Does the field-level filter support the detected data type?
      if (
        fieldDataType !== undefined &&
        filter !== undefined &&
        filter.labels !== undefined &&
        filter.labels["input-types"] !== undefined
      ) {
        filterExpectsDataType =
          filter.labels["input-types"].includes(fieldDataType);
      }
    }

    return (
      <React.Fragment>
        <div className="datacater-context-bar-fixed-element">
          <div className="row w-100 m-0 align-items-center">
            <div className="col p-0">
              <h5 className="mb-0 fw-semibold">
                <FilterIcon className="feather-icon me-2 mt-n1" />
                {filter.name}
              </h5>
            </div>
            <div className="col-auto">
              <button
                className="btn btn-sm btn-outline-danger"
                onClick={(event) => {
                  this.props.handleChangeFunc(
                    event,
                    currentStep,
                    field,
                    "filter",
                    undefined
                  );
                }}
                variant="link"
              >
                Reset
              </button>
            </div>
          </div>
        </div>
        <div className="form-group mb-0 pb-4 datacater-context-bar-function-config">
          <div className="btn-group mb-3" role="group">
            {this.state.currentTab === "config" && (
              <>
                <input
                  type="radio"
                  className="btn-check"
                  name="btnradio"
                  id="config-radio"
                  autoComplete="off"
                  checked
                  readOnly
                />
                <label
                  className="btn btn-outline-primary btn-sm text-white"
                  htmlFor="config-radio"
                >
                  Config
                </label>
              </>
            )}
            {this.state.currentTab !== "config" && (
              <>
                <input
                  onClick={this.selectTab}
                  data-tab="config"
                  type="radio"
                  className="btn-check"
                  name="btnradio"
                  id="config-radio"
                  autoComplete="off"
                />
                <label
                  className="btn btn-outline-primary btn-sm"
                  htmlFor="config-radio"
                >
                  Config
                </label>
              </>
            )}
            {this.state.currentTab === "spec" && (
              <>
                <input
                  type="radio"
                  className="btn-check"
                  name="btnradio"
                  id="spec-radio"
                  autoComplete="off"
                  checked
                  readOnly
                />
                <label
                  className="btn btn-outline-primary btn-sm text-white"
                  htmlFor="spec-radio"
                >
                  Spec
                </label>
              </>
            )}
            {this.state.currentTab !== "spec" && (
              <>
                <input
                  onClick={this.selectTab}
                  data-tab="spec"
                  type="radio"
                  className="btn-check"
                  name="btnradio"
                  id="spec-radio"
                  autoComplete="off"
                />
                <label
                  className="btn btn-outline-primary btn-sm"
                  htmlFor="spec-radio"
                >
                  Spec
                </label>
              </>
            )}
          </div>
          {this.state.currentTab === "config" && (
            <>
              {!filterExpectsDataType && (
                <div className="alert alert-warning">
                  The filter <i>{filter.key}</i> does not support the input type{" "}
                  <i>{fieldDataType}</i>.
                </div>
              )}
              {(filter.config === undefined ||
                Object.keys(filter.config).length === 0) && (
                <div className="alert alert-primary">
                  This filter does not require any configuration.
                </div>
              )}
              {transform === undefined && (
                <div className="alert alert-warning">
                  If you do not combine this filter with a transform, the
                  pipeline will ignore all records that do not pass the filter
                  for further processing.
                </div>
              )}
              {filter.config !== undefined &&
                filter.config.map((configOption, idx) => (
                  <div key={idx}>
                    {[
                      "user-defined-filter",
                      "user-defined-record-filter",
                    ].includes(filter.key) && (
                      <CodeEditor
                        fieldName={field}
                        funcType="filter"
                        currentStep={currentStep}
                        handleChangeFunc={handleChangeFunc}
                        previewState={this.props.previewState}
                        transformStep={transformStep}
                        value={filterConfig[configOption.name] || ""}
                      />
                    )}
                    {![
                      "user-defined-filter",
                      "user-defined-record-filter",
                    ].includes(filter.key) && (
                      <>
                        <label className="mb-2">{configOption.label}:</label>
                        {(configOption.options === undefined ||
                          configOption.options.length === 0) && (
                          <input
                            type="text"
                            className="form-control mb-2"
                            data-fieldname={field}
                            data-currentstep={currentStep}
                            data-prefix="filter"
                            name={configOption.name}
                            onChange={handleChangeFunc}
                            value={filterConfig[configOption.name] || ""}
                          />
                        )}
                        {configOption.options !== undefined &&
                          configOption.options.length > 0 && (
                            <select
                              className="form-select"
                              data-fieldname={field}
                              data-currentstep={currentStep}
                              data-prefix="filter"
                              name={configOption.name}
                              onChange={handleChangeFunc}
                              value={field.filterValue}
                              value={filterConfig[configOption.name] || ""}
                            >
                              {configOption.options.map((option, index) => (
                                <option key={index} value={option}>
                                  {option}
                                </option>
                              ))}
                            </select>
                          )}
                      </>
                    )}
                  </div>
                ))}
            </>
          )}
          {this.state.currentTab === "spec" && (
            <div className="mt-1" style={{ width: "100%" }}>
              <SyntaxHighlighter
                language="yaml"
                showLineNumbers={true}
                showInlineLineNumbers={true}
                customStyle={{
                  fontSize: "0.8rem",
                  margin: "0",
                  background: "#f7fbf8",
                }}
              >
                {jsonToYaml(filter)}
              </SyntaxHighlighter>
            </div>
          )}
        </div>
      </React.Fragment>
    );
  }
}

export default FilterConfig;

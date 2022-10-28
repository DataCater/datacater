import React, { Component } from "react";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import { Filter as FilterIcon, HelpCircle } from "react-feather";
import * as YAML from "json-to-pretty-yaml";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import CodeEditor from "./CodeEditor";

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
    } = this.props;

    const filterConfig =
      pipelineField !== undefined &&
      pipelineField.filter != undefined &&
      pipelineField.filter.config !== undefined
        ? pipelineField.filter.config
        : {};

    const filterExpectsDataType =
      filter !== undefined &&
      filter.labels !== undefined &&
      filter.labels["input-types"] !== undefined
        ? filter.labels["input-types"].includes(fieldDataType)
        : true;

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
              {filter.config !== undefined &&
                filter.config.map((configOption, idx) => (
                  <div key={idx}>
                    {filter.key === "user-defined-filter" && (
                      <CodeEditor
                        fieldName={field}
                        funcType="filter"
                        currentStep={currentStep}
                        handleChangeFunc={handleChangeFunc}
                        previewState={this.props.previewState}
                        value={filterConfig[configOption.name] || ""}
                      />
                    )}
                    {filter.key !== "user-defined-filter" && (
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
            <div className="mt-1" style={{ width: "427px" }}>
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
                {YAML.stringify(filter)}
              </SyntaxHighlighter>
            </div>
          )}
        </div>
      </React.Fragment>
    );
  }
}

export default FilterConfig;

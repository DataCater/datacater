import React, { Component } from "react";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import { HelpCircle, Package } from "react-feather";
import NewFieldForm from "./NewFieldForm";
import CodeEditor from "./CodeEditor";

class Config extends Component {
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
      transforms,
    } = this.props;

    const transformConfig =
      pipelineField !== undefined &&
      pipelineField.transformationConfig != null
        ? pipelineField.transformationConfig
        : {};

    const transformExpectsDataType =
      transform !== undefined &&
      transform.labels !== undefined &&
      transform.labels["input-types"] !== undefined
        ? transform.labels["input-types"].includes(fieldDataType)
        : true;

    const filterConfig =
      pipelineField !== undefined && pipelineField.filterConfig != null
        ? pipelineField.filterConfig
        : {};

    const filterExpectsDataType =
      filter !== undefined &&
      filter.labels !== undefined &&
      filter.labels["input-types"] !== undefined
        ? filter.labels["input-types"].includes(fieldDataType)
        : true;

    /*
    if (transform !== undefined && transformer.key === "add-column") {
      return (
        <NewFieldForm
          field={pipelineField}
          fields={fields}
          handleChangeFunc={handleChangeFunc}
          pipelineStep={pipelineStep}
          sortPosition={sortPosition}
          transforms={transforms}
        />
      );
    }
    */

    return (
      <React.Fragment>
        <div className="datacater-context-bar-fixed-double-element border-bottom">
          <div className="datacater-context-bar-fixed-element w-100">
            <div className="row w-100 m-0 align-items-center">
              <div className="col p-0">
                <h5 className="mb-0 text-nowrap fw-bold">
                  <Package className="feather-icon me-2 mt-n1" />
                  {transform.name}
                </h5>
              </div>
            </div>
          </div>
          <div className="datacater-context-bar-fixed-element w-100 datacater-context-bar-search-field">
            <button
              className="btn btn-outline-danger w-100"
              onClick={(event) => {
                this.props.handleChangeFunc(
                  event,
                  currentStep,
                  pipelineField.fieldName,
                  "transform",
                  undefined
                );
              }}
              variant="link"
            >
              Reset transformation
            </button>
          </div>
        </div>
        <div className="form-group mb-0 py-4 datacater-context-bar-function-config">
          {!transformExpectsDataType && (
            <div className="alert alert-warning">
              The transform <i>{transform.key}</i> does not support the input
              type <i>{fieldDataType}</i>.
            </div>
          )}
          {transform.config !== undefined &&
            transform.config.map((configOption, idx) => (
              <div key={idx}>
                {transform.key === "user-defined-transformation" && (
                  <CodeEditor
                    fieldName={pipelineField.fieldName}
                    currentStep={currentStep}
                    handleChangeFunc={handleChangeFunc}
                    previewState={this.props.previewState}
                    transformationState={this.props.transformationState}
                    value={transformConfig[configOption.name] || ""}
                  />
                )}
                {transform.key !== "user-defined-transformation" && (
                  <>
                    <label className="mb-2">{configOption.label}:</label>
                    {(configOption.options === undefined ||
                      configOption.options.length === 0) && (
                      <input
                        type="text"
                        className="form-control mb-2"
                        data-fieldname={pipelineField.fieldName}
                        data-currentstep={currentStep}
                        data-prefix="transformationConfig"
                        name={configOption.name}
                        onChange={handleChangeFunc}
                        value={transformConfig[configOption.name] || ""}
                      />
                    )}
                    {configOption.options !== undefined &&
                      configOption.options.length > 0 && (
                        <select
                          className="form-select"
                          data-fieldname={pipelineField.fieldName}
                          data-currentstep={currentStep}
                          data-prefix="transformationConfig"
                          name={configOption.name}
                          onChange={handleChangeFunc}
                          value={field.filterValue}
                          value={transformConfig[configOption.name] || ""}
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
          <hr className="mt-4 mb-2" />
          <div className="form-group my-3">
            <label>Restrict transformation:</label>
            <select
              className="form-select mt-2"
              data-fieldname={pipelineField.fieldName}
              data-currentstep={currentStep}
              name="filter"
              onChange={handleChangeFunc}
              value={pipelineField.filter || ""}
            >
              <React.Fragment>
                <option value="">Apply transformation to all values</option>
                {filters.map((filter, index) => (
                  <option key={index} value={filter.key}>
                    {filter.name}
                  </option>
                ))}
              </React.Fragment>
            </select>
            {!filterExpectsDataType && (
              <div className="alert alert-warning mt-4 mb-0">
                The filter <i>{filter.key}</i> does not support the input type{" "}
                <i>{fieldDataType}</i>.
              </div>
            )}
            {filter &&
              filter.config &&
              filter.config.map((configOption, idx) => (
                <div key={idx}>
                  <label className="mt-3 mb-2">{configOption.label}:</label>
                  <input
                    type="text"
                    className="form-control mb-2"
                    data-fieldname={pipelineField.fieldName}
                    data-currentstep={currentStep}
                    data-prefix="filterConfig"
                    name={configOption.name}
                    onChange={handleChangeFunc}
                    value={filterConfig[configOption.name] || ""}
                  />
                </div>
              ))}
          </div>
        </div>
      </React.Fragment>
    );
  }
}

export default Config;

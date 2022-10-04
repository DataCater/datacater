import React, { Component } from "react";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import { HelpCircle, Package } from "react-feather";
import NewAttributeForm from "./NewAttributeForm";
import CodeEditor from "./CodeEditor";

class Config extends Component {
  render() {
    const {
      attribute,
      attributes,
      currentStep,
      editColumn,
      filter,
      filters,
      flattenedDataType,
      handleChangeFunc,
      pipelineAttribute,
      pipelineStep,
      sortPosition,
      transform,
      transforms,
    } = this.props;

    const transformConfig =
      pipelineAttribute !== undefined &&
      pipelineAttribute.transformationConfig != null
        ? pipelineAttribute.transformationConfig
        : {};

    const filterConfig =
      pipelineAttribute !== undefined && pipelineAttribute.filterConfig != null
        ? pipelineAttribute.filterConfig
        : {};

    /*
    if (transform !== undefined && transformer.key === "add-column") {
      return (
        <NewAttributeForm
          attribute={pipelineAttribute}
          attributes={attributes}
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
                  pipelineAttribute.attributeName,
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
          {transform.config !== undefined &&
            transform.config.map((configOption, idx) => (
              <div key={idx}>
                {transform.key === "user-defined-transformation" && (
                  <CodeEditor
                    attributeName={pipelineAttribute.attributeName}
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
                        data-attributename={pipelineAttribute.attributeName}
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
                          data-attributename={pipelineAttribute.attributeName}
                          data-currentstep={currentStep}
                          data-prefix="transformationConfig"
                          name={configOption.name}
                          onChange={handleChangeFunc}
                          value={attribute.filterValue}
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
              data-attributename={pipelineAttribute.attributeName}
              data-currentstep={currentStep}
              name="filter"
              onChange={handleChangeFunc}
              value={pipelineAttribute.filter || ""}
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
            {filter &&
              filter.config &&
              filter.config.map((configOption, idx) => (
                <div key={idx}>
                  <label className="mt-3 mb-2">{configOption.label}:</label>
                  <input
                    type="text"
                    className="form-control mb-2"
                    data-attributename={pipelineAttribute.attributeName}
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

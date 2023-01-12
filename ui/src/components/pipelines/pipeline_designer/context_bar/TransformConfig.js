import React, { Component } from "react";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import { HelpCircle, Package } from "react-feather";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import NewFieldForm from "./NewFieldForm";
import CodeEditor from "./CodeEditor";
import { jsonToYaml } from "../../../../helpers/jsonToYaml";

class TransformConfig extends Component {
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
      transforms,
      transformStep,
    } = this.props;

    let transformConfig = {};
    let transformExpectsDataType = true;

    if (transformStep.kind === "Record") {
      // Load config of the record-level transform
      if (
        transformStep.transform !== undefined &&
        transformStep.transform.config !== undefined
      ) {
        transformConfig = transformStep.transform.config;
      }
    } else {
      // Load config of the field-level transform
      if (
        pipelineField !== undefined &&
        pipelineField.transform !== undefined &&
        pipelineField.transform.config !== undefined
      ) {
        transformConfig = pipelineField.transform.config;
      }

      // Does the field-level transform support the detected data type?
      if (
        transform !== undefined &&
        transform.labels !== undefined &&
        transform.labels["input-types"] !== undefined
      ) {
        transformExpectsDataType =
          transform.labels["input-types"].includes(fieldDataType);
      }
    }

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
        <div className="datacater-context-bar-fixed-element">
          <div className="row w-100 m-0 align-items-center">
            <div className="col p-0">
              <h5 className="mb-0 fw-semibold">
                <Package className="feather-icon me-2 mt-n1" />
                {transform.name}
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
                    "transform",
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
              {!transformExpectsDataType && (
                <div className="alert alert-warning">
                  The transform <i>{transform.key}</i> does not support the
                  input type <i>{fieldDataType}</i>.
                </div>
              )}
              {(transform.config === undefined ||
                Object.keys(transform.config).length === 0) && (
                <div className="alert alert-primary">
                  This transform does not require any configuration.
                </div>
              )}
              {filter !== undefined && (
                <div className="alert alert-warning">
                  This transform is only applied if the configured filter
                  returns <code>True</code>.
                </div>
              )}
              {transform.config !== undefined &&
                transform.config.map((configOption, idx) => (
                  <div key={idx}>
                    {[
                      "user-defined-transformation",
                      "user-defined-record-transformation",
                    ].includes(transform.key) && (
                      <CodeEditor
                        fieldName={field}
                        funcType="transform"
                        currentStep={currentStep}
                        handleChangeFunc={handleChangeFunc}
                        previewState={this.props.previewState}
                        transformStep={transformStep}
                        value={transformConfig[configOption.name] || ""}
                      />
                    )}
                    {![
                      "user-defined-transformation",
                      "user-defined-record-transformation",
                    ].includes(transform.key) && (
                      <>
                        <label className="mb-2">{configOption.label}:</label>
                        {(configOption.options === undefined ||
                          configOption.options.length === 0) && (
                          <input
                            type="text"
                            className="form-control mb-2"
                            data-fieldname={field}
                            data-currentstep={currentStep}
                            data-prefix="transform"
                            name={configOption.name}
                            onChange={handleChangeFunc}
                            value={transformConfig[configOption.name] || ""}
                          />
                        )}
                        {configOption.options !== undefined &&
                          configOption.options.length > 0 && (
                            <select
                              className="form-select"
                              data-fieldname={field}
                              data-currentstep={currentStep}
                              data-prefix="transform"
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
                {jsonToYaml(transform)}
              </SyntaxHighlighter>
            </div>
          )}
        </div>
      </React.Fragment>
    );
  }
}

export default TransformConfig;

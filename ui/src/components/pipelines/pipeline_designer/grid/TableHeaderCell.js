import React, { Component } from "react";
import { Button, Modal } from "react-bootstrap";
import { ArrowRight, Code, Filter, Key, Package, Trash } from "react-feather";
import DataTypeIcon from "./DataTypeIcon";
import FrequentValues from "../../../data_profiler/FrequentValues";
import AttributeStats from "../../../data_profiler/AttributeStats";
import "../../../../scss/grid/statistics.scss";

class TableHeaderCell extends Component {
  constructor(props) {
    super(props);

    this.openContextBar = this.openContextBar.bind(this);
    this.renderField = this.renderField.bind(this);
  }

  renderFieldName(field) {
    if (field.oldName === undefined) {
      return (
        <div className="d-flex align-items-center">
          {[undefined, ""].includes(field.name) && (
            <span className="font-italic">
              <DataTypeIcon dataType={field.dataType} /> Untitled field
            </span>
          )}
          {![undefined, ""].includes(field.name) && (
            <span>
              <DataTypeIcon dataType={field.dataType} /> {field.name}
            </span>
          )}
        </div>
      );
    } else {
      return (
        <div className="d-flex align-items-center">
          {field.oldName}
          <ArrowRight className="feather-icon" />
          {field.name}
        </div>
      );
    }
  }

  openContextBar(field) {
    this.props.column.editColumnFunc(field.name, this.props.column.currentStep);
  }

  renderField(field, idx) {
    const {
      filterOfCurrentStep,
      fieldName,
      currentPage,
      currentStep,
      filters,
      introducedFields,
      removeColumnFunc,
      step,
      transforms,
    } = this.props.column;

    let sampleCellClassNames = "sample-cell py-2 ps-0";
    let transformation = undefined;

    if (
      currentStep >= 0 &&
      step.fields !== undefined &&
      step.fields[field.name] !== undefined &&
      step.fields[field.name].transform !== undefined
    ) {
      const transformationKey = step.fields[field.name].transform.key;
      transformation = this.props.column.transforms.find(
        (transformation) => transformation.key === transformationKey
      );
    }

    return (
      <React.Fragment>
        <div className={sampleCellClassNames}>
          <div className="row m-0 align-items-center">
            <div className="col overflow-hidden text-nowrap d-flex align-items-center ps-0 pe-2">
              {this.renderFieldName(field)}
            </div>
            <div className="col-auto d-flex align-items-center px-2">
              {introducedFields.includes(fieldName) && (
                <Trash
                  className="feather-icon clickable me-1"
                  data-field-name={fieldName}
                  onClick={removeColumnFunc}
                  title="Delete Virtual Field"
                />
              )}
            </div>
          </div>
          {step !== undefined &&
            step.kind === "Field" &&
            transformation == null && (
              <Button
                variant="white"
                size="sm"
                className="w-100 text-left mt-2 d-flex align-items-center text-nowrap btn-outline-primary"
                onClick={(event) => this.openContextBar(field)}
              >
                <Package className="feather-icon me-2" />
                Apply transform
              </Button>
            )}
          {step !== undefined &&
            step.kind === "Field" &&
            transformation != null && (
              <Button
                variant="primary"
                size="sm"
                className="w-100 text-left mt-2 d-flex align-items-center text-nowrap text-white"
                onClick={(event) => this.openContextBar(field)}
              >
                <Package className="feather-icon me-2" />
                {transformation.name}
              </Button>
            )}
        </div>
        <div className="datacater-stats-content datacater-most-frequent-values-content">
          {!["array", "object"].includes(field.dataType) && (
            <FrequentValues fieldProfile={field} />
          )}
          {["array", "object"].includes(field.dataType) && (
            <span className="text-black-50">
              Most frequent values are not available
            </span>
          )}
        </div>
      </React.Fragment>
    );
  }

  render() {
    const { className, column } = this.props;

    const { field } = column;

    if (column.isRowNumber) {
      const numberOfColumns =
        this.props.container.columnManager._columns.length - 1;
      return (
        <div className={className}>
          <div className="d-flex align-items-center justify-content-center">
            {numberOfColumns} fields
          </div>
        </div>
      );
    } else {
      return (
        <div className={className + " px-2"}>
          {this.renderField(field, field.id)}
        </div>
      );
    }
  }
}

export default TableHeaderCell;

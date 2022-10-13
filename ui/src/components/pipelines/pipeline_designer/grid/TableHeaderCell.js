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
    this.renderAttribute = this.renderAttribute.bind(this);
  }

  renderAttributeName(attribute) {
    if (attribute.oldName === undefined) {
      return (
        <div className="d-flex align-items-center">
          {[undefined, ""].includes(attribute.name) && (
            <span className="font-italic">
              <DataTypeIcon dataType={attribute.dataType} /> Untitled attribute
            </span>
          )}
          {![undefined, ""].includes(attribute.name) && (
            <span>
              <DataTypeIcon dataType={attribute.dataType} /> {attribute.name}
            </span>
          )}
        </div>
      );
    } else {
      return (
        <div className="d-flex align-items-center">
          {attribute.oldName}
          <ArrowRight className="feather-icon" />
          {attribute.name}
        </div>
      );
    }
  }

  openContextBar(attribute) {
    const { currentPage, editColumnFunc } = this.props.column;

    if (currentPage === "filter") {
      editColumnFunc(attribute.name, undefined, "filter");
    } else if (currentPage === "transform") {
      editColumnFunc(
        attribute.name,
        this.props.column.currentStep,
        "transform"
      );
    }
  }

  renderAttribute(attribute, idx) {
    const {
      filterOfCurrentStep,
      attributeName,
      currentPage,
      currentStep,
      filters,
      introducedAttributes,
      removeColumnFunc,
      transforms,
      transformationOfCurrentStep,
    } = this.props.column;

    let sampleCellClassNames = "sample-cell py-2 ps-0";

    let filter = undefined;

    if (
      currentPage === "filter" &&
      filterOfCurrentStep !== undefined &&
      ![undefined, ""].includes(filterOfCurrentStep.filter)
    ) {
      const filterKey = filterOfCurrentStep.filter;
      filter = filters.find((filter) => filter.key === filterKey);
    }

    let transformation = undefined;
    if (
      currentStep >= 0 &&
      transformationOfCurrentStep !== undefined &&
      ![undefined, ""].includes(transformationOfCurrentStep.transformation)
    ) {
      const transformationKey = transformationOfCurrentStep.transformation;
      transformation = this.props.column.transforms.find(
        (transformation) => transformation.key === transformationKey
      );
    }

    return (
      <React.Fragment>
        <div className={sampleCellClassNames}>
          <div className="row m-0 align-items-center">
            <div className="col overflow-hidden text-nowrap d-flex align-items-center ps-0 pe-2">
              {this.renderAttributeName(attribute)}
            </div>
            <div className="col-auto d-flex align-items-center px-2">
              {introducedAttributes.includes(attributeName) && (
                <Trash
                  className="feather-icon clickable me-1"
                  data-attribute-name={attributeName}
                  onClick={removeColumnFunc}
                  title="Delete Virtual Attribute"
                />
              )}
            </div>
          </div>
          {currentPage === "filter" && filter == null && (
            <Button
              variant="white"
              size="sm"
              className="w-100 text-left mt-2 d-flex align-items-center text-nowrap btn-outline-primary"
              onClick={(event) => this.openContextBar(attribute)}
            >
              <Filter className="feather-icon me-2" />
              Apply filter
            </Button>
          )}
          {currentPage === "filter" && filter != null && (
            <Button
              variant="primary"
              size="sm"
              className="w-100 text-left mt-2 d-flex align-items-center text-nowrap text-white"
              onClick={(event) => this.openContextBar(attribute)}
            >
              <Filter className="feather-icon me-2" />
              {filter.name}
            </Button>
          )}
          {currentPage === "transform" && transformation == null && (
            <Button
              variant="white"
              size="sm"
              className="w-100 text-left mt-2 d-flex align-items-center text-nowrap btn-outline-primary"
              onClick={(event) => this.openContextBar(attribute)}
            >
              <Package className="feather-icon me-2" />
              Apply transformation
            </Button>
          )}
          {currentPage === "transform" && transformation != null && (
            <Button
              variant="primary"
              size="sm"
              className="w-100 text-left mt-2 d-flex align-items-center text-nowrap text-white"
              onClick={(event) => this.openContextBar(attribute)}
            >
              <Package className="feather-icon me-2" />
              {transformation.name}
            </Button>
          )}
        </div>
        <div className="datacater-stats-content datacater-most-frequent-values-content">
          {!["array", "object"].includes(attribute.dataType) && (
            <FrequentValues attributeProfile={attribute} />
          )}
          {["array", "object"].includes(attribute.dataType) && (
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

    const { attribute } = column;

    if (column.isRowNumber) {
      const numberOfColumns =
        this.props.container.columnManager._columns.length - 1;
      return (
        <div className={className}>
          <div className="d-flex align-items-center justify-content-center">
            {numberOfColumns} attributes
          </div>
        </div>
      );
    } else {
      return (
        <div className={className + " px-2"}>
          {this.renderAttribute(attribute, attribute.id)}
        </div>
      );
    }
  }
}

export default TableHeaderCell;

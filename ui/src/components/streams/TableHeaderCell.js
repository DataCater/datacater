import React, { Component } from "react";
import { Check, Code, Hash, HelpCircle, List, Type } from "react-feather";
import FrequentValues from "../data_profiler/FrequentValues";

class TableHeaderCell extends Component {
  render() {
    const { className, column } = this.props;

    const { attribute, attributeName, idx } = column;

    let dataTypeIcon = <HelpCircle className="feather-icon" />;

    switch (attribute.dataType) {
      case "array":
        dataTypeIcon = (
          <span title="Array">
            <List className="feather-icon" />
          </span>
        );
        break;
      case "object":
        dataTypeIcon = (
          <span title="Object">
            <Code className="feather-icon" />
          </span>
        );
        break;
      case "boolean":
        dataTypeIcon = (
          <span title="Boolean">
            <Check className="feather-icon" />
          </span>
        );
        break;
      case "number":
        dataTypeIcon = (
          <span title="Number">
            <Hash className="feather-icon" />
          </span>
        );
        break;
      case "string":
        dataTypeIcon = (
          <span title="String">
            <Type className="feather-icon" />
          </span>
        );
        break;
      default:
        break;
    }

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
          <div className="sample-cell py-2 px-1" key={idx}>
            <div className="d-flex align-items-center w-100 h-100 text-break overflow-hidden">
              {dataTypeIcon} {attributeName}
            </div>
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
        </div>
      );
    }
  }
}

export default TableHeaderCell;

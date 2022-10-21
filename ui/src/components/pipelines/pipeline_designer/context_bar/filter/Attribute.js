import React, { Component } from "react";
import { Filter as FilterIcon, HelpCircle } from "react-feather";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import DataTypeIcon from "../../grid/DataTypeIcon";
import AttributeFilterList from "./AttributeFilterList";

class Attribute extends Component {
  render() {
    const { field, filter, filters, handleChangeFunc, profile } = this.props;

    const fieldProfile = profile[field];
    const fieldDataType = fieldProfile.dataType;

    const currentFilter =
      filter.filter === ""
        ? undefined
        : filters.find((f) => f.key === filter.filter);

    const filterIsDefined = ![undefined, ""].includes(currentFilter);

    const filterConfig =
      filter !== undefined && filter.filterConfig !== undefined
        ? filter.filterConfig
        : {};

    const filterExpectsDataType =
      currentFilter !== undefined &&
      currentFilter.labels !== undefined &&
      currentFilter.labels["input-types"] !== undefined
        ? currentFilter.labels["input-types"].includes(fieldDataType)
        : true;

    return (
      <React.Fragment>
        <div className="row border-bottom py-4">
          <div className="col ps-0">
            <h3 className="mb-0 overflow-hidden text-nowrap d-flex align-items-center">
              <DataTypeIcon dataType={fieldProfile.dataType} />{" "}
              <span className="ms-1">{field}</span>
            </h3>
          </div>
        </div>

        {!filterIsDefined && (
          <AttributeFilterList
            field={field}
            fieldDataType={fieldProfile.dataType}
            filter={filter}
            filters={filters}
            handleChangeFunc={handleChangeFunc}
          />
        )}

        {filterIsDefined && (
          <React.Fragment>
            <div className="datacater-context-bar-fixed-double-element border-bottom">
              <div className="datacater-context-bar-fixed-element w-100">
                <div className="row w-100 m-0 align-items-center">
                  <div className="col p-0">
                    <h5 className="mb-0 text-nowrap">
                      <FilterIcon className="feather-icon me-2 mt-n1" />
                      {currentFilter.name}
                    </h5>
                  </div>
                </div>
              </div>
              <div className="datacater-context-bar-fixed-element w-100 datacater-context-bar-search-field">
                <button
                  onClick={(event) => {
                    this.props.handleChangeFunc(
                      event,
                      field,
                      "filter",
                      undefined
                    );
                  }}
                  type="button"
                  className="btn btn-outline-danger w-100"
                >
                  Reset filter
                </button>
              </div>
            </div>
            <div className="form-group mb-0 py-4 datacater-context-bar-function-config">
              {!filterExpectsDataType && (
                <div className="alert alert-warning">
                  The filter <i>{filter.key}</i> does not support the input type{" "}
                  <i>{fieldDataType}</i>.
                </div>
              )}
              {currentFilter &&
                currentFilter.config &&
                currentFilter.config.map((configOption, idx) => (
                  <div key={idx}>
                    <label className="mb-2">{configOption.label}:</label>
                    <input
                      type="text"
                      className="form-control mb-2"
                      data-fieldname={this.props.field}
                      name={configOption.name}
                      onChange={this.props.handleChangeFunc}
                      value={filterConfig[configOption.name] || ""}
                    />
                  </div>
                ))}
            </div>
          </React.Fragment>
        )}
      </React.Fragment>
    );
  }
}

export default Attribute;

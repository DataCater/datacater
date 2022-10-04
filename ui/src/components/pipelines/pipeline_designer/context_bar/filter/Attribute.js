import React, { Component } from "react";
import { Filter as FilterIcon, HelpCircle } from "react-feather";
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap";
import AttributeFilterList from "./AttributeFilterList";

class Attribute extends Component {
  render() {
    const { attribute, filter, filters, handleChangeFunc } = this.props;

    const currentFilter =
      filter.filter === ""
        ? undefined
        : filters.find((f) => f.key === filter.filter);

    const filterIsDefined = ![undefined, ""].includes(currentFilter);

    const filterConfig =
      filter !== undefined && filter.filterConfig !== undefined
        ? filter.filterConfig
        : {};

    return (
      <React.Fragment>
        <div className="row border-bottom py-4">
          <div className="col ps-0">
            <h3 className="fw-bold mb-0 text-nowrap">{attribute}</h3>
          </div>
        </div>

        {!filterIsDefined && (
          <AttributeFilterList
            filter={filter}
            attribute={attribute}
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
                      attribute,
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
              {currentFilter &&
                currentFilter.config &&
                currentFilter.config.map((configOption, idx) => (
                  <div key={idx}>
                    <label className="mb-2">{configOption.label}:</label>
                    <input
                      type="text"
                      className="form-control mb-2"
                      data-attributename={this.props.attribute}
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

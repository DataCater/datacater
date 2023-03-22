import React, { Component } from "react";
import { humanizeDataType } from "../../../../helpers/humanizeDataType";
import AttributeTransformationList from "./AttributeTransformationList";
import AttributeFilterList from "./AttributeFilterList";
import DataTypeIcon from "../grid/DataTypeIcon";
import TransformConfig from "./TransformConfig";
import FilterConfig from "./FilterConfig";
import ContextBarSizer from "./ContextBarSizer";

class Attribute extends Component {
  constructor(props) {
    super(props);

    // If the step defines a filter but no transfor, open the filter by default
    // If not, open the transform by default
    const field = props.fields.find(
      (attr) => attr === props.editColumn.fieldName
    );
    const pipelineField = props.transformStep.fields[field];
    const currentTab =
      pipelineField !== undefined &&
      pipelineField.transform === undefined &&
      pipelineField.filter !== undefined
        ? "filter"
        : "transform";

    this.state = {
      currentTab: currentTab,
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
      changeContextBarSizeFunc,
      contextBarSize,
      currentStep,
      editColumn,
      fields,
      filters,
      handleChangeFunc,
      profile,
      sortPosition,
      transformStep,
      transforms,
    } = this.props;

    const field = fields.find((attr) => attr === editColumn.fieldName);
    const fieldProfile = profile[field] || {};
    const pipelineField = transformStep.fields[field];

    let transform = undefined;
    let filter = undefined;
    if (pipelineField !== undefined) {
      const transformKey =
        pipelineField.transform !== undefined
          ? pipelineField.transform.key
          : undefined;
      transform = transforms.find(
        (transform) => transform.key === transformKey
      );
      const filterKey =
        pipelineField.filter !== undefined
          ? pipelineField.filter.key
          : undefined;
      filter = filters.find((filter) => filter.key === filterKey);
    }

    return (
      <React.Fragment>
        <div className="row py-4">
          <div className="col overflow-hidden">
            <h4 className="mb-0 overflow-hidden text-nowrap d-flex align-items-center fw-bold">
              <DataTypeIcon dataType={fieldProfile.dataType} />{" "}
              <span className="ms-2">{field}</span>
            </h4>
          </div>
          <div className="col-auto ps-0">
            <ContextBarSizer
              changeContextBarSizeFunc={changeContextBarSizeFunc}
              contextBarSize={contextBarSize}
            />
          </div>
        </div>
        <ul className="nav nav-tabs">
          <li className="nav-item">
            {this.state.currentTab === "transform" && (
              <a
                className="nav-link active text-black fw-bold"
                aria-current="page"
                href="#"
              >
                Transform
              </a>
            )}
            {this.state.currentTab !== "transform" && (
              <a
                onClick={this.selectTab}
                data-tab="transform"
                className="nav-link"
                aria-current="page"
                href="#"
              >
                Transform
              </a>
            )}
          </li>
          <li className="nav-item">
            {this.state.currentTab === "filter" && (
              <a
                className="nav-link active text-black fw-bold"
                aria-current="page"
                href="#"
              >
                Filter
              </a>
            )}
            {this.state.currentTab !== "filter" && (
              <a
                onClick={this.selectTab}
                data-tab="filter"
                className="nav-link"
                aria-current="page"
                href="#"
              >
                Filter
              </a>
            )}
          </li>
        </ul>
        {this.state.currentTab === "transform" && (
          <>
            {transform == null && (
              <AttributeTransformationList
                field={field}
                fieldDataType={fieldProfile.dataType}
                currentStep={currentStep}
                handleChangeFunc={handleChangeFunc}
                transformStep={transformStep}
                transformersForField={transforms}
              />
            )}
            {transform != null && (
              <TransformConfig
                field={field}
                fields={fields}
                fieldDataType={fieldProfile.dataType}
                currentStep={currentStep}
                editColumn={editColumn}
                filter={filter}
                filters={filters}
                handleChangeFunc={handleChangeFunc}
                pipelineField={pipelineField}
                previewState={this.props.previewState}
                sortPosition={sortPosition}
                transform={transform}
                transforms={transforms}
                transformStep={transformStep}
              />
            )}
          </>
        )}
        {this.state.currentTab === "filter" && (
          <>
            {filter == null && (
              <AttributeFilterList
                currentStep={currentStep}
                field={field}
                fieldDataType={fieldProfile.dataType}
                filter={filter}
                filters={filters}
                handleChangeFunc={handleChangeFunc}
                transformStep={transformStep}
              />
            )}
            {filter != null && (
              <FilterConfig
                field={field}
                fields={fields}
                fieldDataType={fieldProfile.dataType}
                currentStep={currentStep}
                editColumn={editColumn}
                filter={filter}
                filters={filters}
                handleChangeFunc={handleChangeFunc}
                pipelineField={pipelineField}
                previewState={this.props.previewState}
                sortPosition={sortPosition}
                transform={transform}
                transformStep={transformStep}
              />
            )}
          </>
        )}
      </React.Fragment>
    );
  }
}

export default Attribute;

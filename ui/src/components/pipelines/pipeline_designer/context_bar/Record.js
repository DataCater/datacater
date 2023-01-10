import React, { Component } from "react";
import TransformationList from "./AttributeTransformationList";
import FilterList from "./AttributeFilterList";
import TransformConfig from "./TransformConfig";
import FilterConfig from "./FilterConfig";

class Record extends Component {
  constructor(props) {
    super(props);

    // If the step defines a filter but no transform, open the filter by default
    // If not, open the transform by default
    const currentTab =
      props.transformStep !== undefined &&
      props.transformStep.transform === undefined &&
      props.transformStep.filter !== undefined
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
      fields,
      currentStep,
      editColumn,
      filters,
      handleChangeFunc,
      sortPosition,
      transformStep,
      transforms,
    } = this.props;

    let transform = undefined;
    let filter = undefined;
    if (transformStep !== undefined) {
      const transformKey =
        transformStep.transform !== undefined
          ? transformStep.transform.key
          : undefined;
      transform = transforms.find(
        (transform) => transform.key === transformKey
      );
      const filterKey =
        transformStep.filter !== undefined
          ? transformStep.filter.key
          : undefined;
      filter = filters.find((filter) => filter.key === filterKey);
    }

    return (
      <React.Fragment>
        <div className="row py-4">
          <div className="col">
            <h3 className="mb-0 overflow-hidden text-nowrap d-flex align-items-center fw-bold">
              Record
            </h3>
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
              <TransformationList
                currentStep={currentStep}
                handleChangeFunc={handleChangeFunc}
                transformStep={transformStep}
                transformersForField={transforms}
              />
            )}
            {transform != null && (
              <TransformConfig
                currentStep={currentStep}
                editColumn={editColumn}
                filter={filter}
                filters={filters}
                handleChangeFunc={handleChangeFunc}
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
              <FilterList
                currentStep={currentStep}
                filter={filter}
                filters={filters}
                handleChangeFunc={handleChangeFunc}
                transformStep={transformStep}
              />
            )}
            {filter != null && (
              <FilterConfig
                currentStep={currentStep}
                editColumn={editColumn}
                filter={filter}
                filters={filters}
                handleChangeFunc={handleChangeFunc}
                sortPosition={sortPosition}
                transformStep={transformStep}
              />
            )}
          </>
        )}
      </React.Fragment>
    );
  }
}

export default Record;

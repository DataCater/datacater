import React, { Component } from "react";
import BaseTable, { AutoResizer } from "react-base-table";
import TableHeaderCell from "./grid/TableHeaderCell";
import { renderTableCellContent } from "../../../helpers/renderTableCellContent";
import "../../../scss/grid.scss";

class Grid extends Component {
  constructor(props) {
    super(props);
    this.state = {
      scrollLeft: 0,
      scrollTop: 0,
    };

    this.setRef = this.setRef.bind(this);
    this.getCell = this.getCell.bind(this);
    this.getCellProps = this.getCellProps.bind(this);
    this.saveScrollPosition = this.saveScrollPosition.bind(this);
  }

  getCell(sample, attribute) {
    let classNames = "sample-cell w-100 text-nowrap";
    if (
      sample.lastChange !== undefined &&
      sample.lastChange[attribute] !== undefined
    ) {
      if (sample.lastChange[attribute] === this.props.currentStep) {
        classNames += " changed-in-current-step";
      } else {
        classNames += " changed-in-previous-step";
      }
    }

    if (
      this.props.editColumnAttribute !== undefined &&
      this.props.editColumnAttribute.name === attribute
    ) {
      classNames += " sample-cell-editing";
    }

    const rawValue = sample[attribute.name];

    return (
      <div className={classNames} key={attribute.name} title={"" + rawValue}>
        {renderTableCellContent(rawValue)}
      </div>
    );
  }

  getCellProps({ columnIndex }) {
    return {
      "data-col-idx": columnIndex,
    };
  }

  getColumn(attribute, idx, filters, defaultColumnWidth) {
    const me = this;

    const filterOfCurrentStep = this.props.pipeline.spec.filters.find(
      (filter) => filter.attributeName === attribute.name
    );

    const pipelineStep =
      this.props.pipeline.spec.transformationSteps[this.props.currentStep];
    let transformationOfCurrentStep = undefined;
    if (pipelineStep !== undefined) {
      transformationOfCurrentStep = pipelineStep.transformations.find(
        (transformation) => transformation.attributeName === attribute.name
      );
    }

    return {
      // column properties
      title: attribute.name,
      width: attribute.id === 0 ? 50 : defaultColumnWidth,
      key: parseInt(attribute.id),
      frozen: attribute.id === 0 ? "left" : false,
      resizable: true,
      dataGetter: ({ rowData, rowIndex }) => {
        return attribute.id !== 0 ? rowData[attribute.name] : rowIndex + 1;
      },
      cellRenderer: function ({
        cellData,
        columns,
        column,
        columnIndex,
        rowData,
        rowIndex,
        container,
        isScrolling,
      }) {
        if (!column.isRowNumber && column.attribute !== undefined) {
          return me.getCell(rowData, column.attribute);
        } else {
          return cellData;
        }
      },
      // custom properties used for header sub components
      isRowNumber: attribute.id === 0,
      attribute: attribute,
      filters: this.props.filters,
      attributes: this.props.attributes,
      attributeProfiles: this.props.profile,
      filterOfCurrentStep: filterOfCurrentStep,
      pipelineStep: this.props.pipelineStep,
      currentStep: this.props.currentStep,
      currentPage: this.props.currentPage,
      introducedAttributes: this.props.introducedAttributes,
      editColumnAttribute: this.props.editColumnAttribute,
      editColumnFunc: this.props.editColumnFunc,
      removeColumnFunc: this.props.removeColumnFunc,
      handlePipelineStepChangeFunc: this.props.handlePipelineStepChangeFunc,
      handleChangeFunc: this.props.handleFilterChangeFunc,
      showStatistics: this.props.showStatistics,
      transformationOfCurrentStep: transformationOfCurrentStep,
      transforms: this.props.transforms,
    };
  }

  getColumns() {
    const filters = []; // TODO: Filter.getFilters();
    let columnsConfig = [...this.props.attributes];
    let columns = [];

    const firstColumnWidth = 50;
    let defaultColumnWidth = 300;

    // check whether columns fill width of screen
    // If not, increase the default column width
    const availableWidth = window.screen.width - firstColumnWidth - 3;
    if (defaultColumnWidth * this.props.attributes.length < availableWidth) {
      defaultColumnWidth = availableWidth / this.props.attributes.length;
    }

    // add # row number column
    columnsConfig = columnsConfig.map((attributeName, idx) =>
      Object.assign({}, this.props.profile[attributeName], {
        id: idx + 1,
        name: attributeName,
      })
    );
    columnsConfig.unshift({ id: 0, name: "#" });
    columnsConfig.map((attribute, idx) =>
      columns.push(this.getColumn(attribute, idx, filters, defaultColumnWidth))
    );

    return columns;
  }

  getData() {
    // filter records which were dropped
    const sampleRecords = this.props.sampleRecords.filter(
      (record) => record.isDropped !== true
    );

    // we have to make sure that the samples get an id property. this is needed for row hover / selection
    sampleRecords.forEach(function (sample, index) {
      sample["id"] = index + 1;
    });

    return sampleRecords;
  }

  calculateHeaderHeight() {
    const { currentPage } = this.props;
    let headerHeight = 153;

    // filters or pipeline steps (for Apply filter/transformation button)
    if (["filter", "transform"].includes(currentPage)) {
      headerHeight += 40;
    }

    return headerHeight;
  }

  getClassNames(columns) {
    const { currentStep, editColumnAttribute } = this.props;
    let classNames;
    let activeColumnIndex;

    // highlight active column
    if (editColumnAttribute !== undefined) {
      activeColumnIndex = columns.findIndex(function (column) {
        return (
          column.attribute !== undefined &&
          parseInt(column.attribute.id) === parseInt(editColumnAttribute.id)
        );
      });

      if (activeColumnIndex) {
        classNames = `active-col-${activeColumnIndex}`;
      }
    }

    if (currentStep === 0) {
      classNames += " datacater-grid-filters";
    } else if (currentStep > 0) {
      classNames += " datacater-grid-pipeline-steps";
    }

    return classNames;
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    // scroll to last known position
    document.querySelector(".datacater-grid__body").scrollLeft =
      this.state.scrollLeft;
    document.querySelector(".datacater-grid__body").scrollTop =
      this.state.scrollTop;

    if (this.props.addedColumn) {
      this.grid.scrollToLeft(this.grid.getTotalColumnsWidth());
    }
  }

  saveScrollPosition({
    scrollLeft,
    scrollTop,
    horizontalScrollDirection,
    verticalScrollDirection,
    scrollUpdateWasRequested,
  }) {
    // persist scroll position without re-rendering component
    this.state.scrollLeft = scrollLeft;
    this.state.scrollTop = scrollTop;
  }

  setRef(ref) {
    this.grid = ref;
  }

  render() {
    const columns = this.getColumns();
    let classNames = this.getClassNames(columns);
    const data = this.getData();

    const gridTransformClassName = (this.props.currentPage === "transform")
      ? " datacater-grid-container-transform"
      : "";

    return (
      <div className="container-fluid">
        <div className="row mt-2">
          <div
            className={`col-12 w-100 px-0 datacater-grid-container${gridTransformClassName}`}
            style={{ position: "relative" }}
          >
            <AutoResizer>
              {({ width, height }) => (
                <BaseTable
                  classPrefix="datacater-grid"
                  columns={this.getColumns(this.props.profile)}
                  components={{ TableHeaderCell }}
                  data={data}
                  fixed
                  headerHeight={this.calculateHeaderHeight()}
                  headerCellProps={this.getCellProps}
                  height={height}
                  rowHeight={24}
                  ref={this.setRef}
                  width={width}
                  cellProps={this.getCellProps}
                  onScroll={this.saveScrollPosition}
                />
              )}
            </AutoResizer>
          </div>
        </div>
      </div>
    );
  }
}

export default Grid;

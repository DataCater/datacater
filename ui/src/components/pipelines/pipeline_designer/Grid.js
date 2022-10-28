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

  getCell(sample, field) {
    let classNames = "sample-cell w-100 text-nowrap";
    if (
      sample.lastChange !== undefined &&
      sample.lastChange[field] !== undefined
    ) {
      if (sample.lastChange[field] === this.props.currentStep) {
        classNames += " changed-in-current-step";
      } else {
        classNames += " changed-in-previous-step";
      }
    }

    if (
      this.props.editColumnField !== undefined &&
      this.props.editColumnField.name === field
    ) {
      classNames += " sample-cell-editing";
    }

    const rawValue = sample[field.name];

    return (
      <div className={classNames} key={field.name} title={"" + rawValue}>
        {renderTableCellContent(rawValue)}
      </div>
    );
  }

  getCellProps({ columnIndex }) {
    return {
      "data-col-idx": columnIndex,
    };
  }

  getColumn(field, idx, filters, defaultColumnWidth) {
    const me = this;

    return {
      // column properties
      title: field.name,
      width: field.id === 0 ? 50 : defaultColumnWidth,
      key: parseInt(field.id),
      frozen: field.id === 0 ? "left" : false,
      resizable: true,
      dataGetter: ({ rowData, rowIndex }) => {
        return field.id !== 0 ? rowData[field.name] : rowIndex + 1;
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
        if (!column.isRowNumber && column.field !== undefined) {
          return me.getCell(rowData, column.field);
        } else {
          return cellData;
        }
      },
      // custom properties used for header sub components
      isRowNumber: field.id === 0,
      field: field,
      filters: this.props.filters,
      fields: this.props.fields,
      fieldProfiles: this.props.profile,
      pipelineStep: this.props.pipelineStep,
      currentStep: this.props.currentStep,
      currentPage: this.props.currentPage,
      introducedFields: this.props.introducedFields,
      editColumnField: this.props.editColumnField,
      editColumnFunc: this.props.editColumnFunc,
      removeColumnFunc: this.props.removeColumnFunc,
      handlePipelineStepChangeFunc: this.props.handlePipelineStepChangeFunc,
      handleChangeFunc: this.props.handleFilterChangeFunc,
      showStatistics: this.props.showStatistics,
      step: this.props.step,
      transforms: this.props.transforms,
    };
  }

  getColumns() {
    const filters = [];
    let columnsConfig = [...this.props.fields];
    let columns = [];

    const firstColumnWidth = 50;
    let defaultColumnWidth = 300;

    // check whether columns fill width of browser
    // If not, increase the default column width
    const availableWidth = window.innerWidth - firstColumnWidth - 3;
    if (defaultColumnWidth * this.props.fields.length < availableWidth) {
      defaultColumnWidth = availableWidth / this.props.fields.length;
    }

    // add # row number column
    columnsConfig = columnsConfig.map((fieldName, idx) =>
      Object.assign({}, this.props.profile[fieldName], {
        id: idx + 1,
        name: fieldName,
      })
    );
    columnsConfig.unshift({ id: 0, name: "#" });
    columnsConfig.map((field, idx) =>
      columns.push(this.getColumn(field, idx, filters, defaultColumnWidth))
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
    const { currentStep, editColumnField } = this.props;
    let activeColumnIndex;

    // highlight active column
    if (editColumnField !== undefined) {
      activeColumnIndex = columns.findIndex(function (column) {
        return (
          column.field !== undefined && column.field.name === editColumnField
        );
      });

      if (activeColumnIndex) {
        return `active-col-${activeColumnIndex}`;
      }
    }

    return "";
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

    const gridTransformClassName =
      this.props.currentPage === "transform"
        ? " datacater-grid-container-transform"
        : "";

    return (
      <div className="container-fluid">
        <div className="row">
          <div
            className={`col-12 w-100 px-0 datacater-grid-container${gridTransformClassName}`}
            style={{ position: "relative" }}
          >
            <AutoResizer>
              {({ width, height }) => (
                <BaseTable
                  classPrefix="datacater-grid"
                  className={classNames}
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

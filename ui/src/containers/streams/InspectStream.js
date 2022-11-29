import React, { Component } from "react";
import { connect } from "react-redux";
import {
  Check,
  Code,
  Hash,
  HelpCircle,
  List,
  Table,
  Type,
} from "react-feather";
import BaseTable, { AutoResizer } from "react-base-table";
import { fetchStream, inspectStream } from "../../actions/streams";
import { profileRecords } from "../../helpers/profileRecords";
import TableHeaderCell from "../../components/streams/TableHeaderCell";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { renderTableCellContent } from "../../helpers/renderTableCellContent";
import "../../scss/grid.scss";
import "../../scss/grid/statistics.scss";

class InspectStream extends Component {
  constructor(props) {
    super(props);
    this.state = {
      inspectLimit: 100,
      showApiCall: false,
      showGrid: true,
    };
    this.toggleShowGrid = this.toggleShowGrid.bind(this);
    this.getColumns = this.getColumns.bind(this);
    this.updateInspectLimit = this.updateInspectLimit.bind(this);
    this.updateShowApiCall = this.updateShowApiCall.bind(this);
  }

  getColumns(profile) {
    let columnConfigs = [];

    const fieldNames = Object.keys(profile);

    columnConfigs.unshift({ id: 0, name: "#" });

    fieldNames.forEach((fieldName, idx) => {
      columnConfigs.push(
        Object.assign({}, profile[fieldName], {
          id: idx + 1,
          name: fieldName,
        })
      );
    });

    const firstColumnWidth = 50;
    let defaultColumnWidth = 300;

    // check whether columns fill width of screen
    // If not, increase the default column width
    const availableWidth = window.innerWidth - firstColumnWidth - 3;
    if (defaultColumnWidth * fieldNames.length < availableWidth) {
      defaultColumnWidth = availableWidth / fieldNames.length;
    }

    let columns = [];
    columnConfigs.forEach((columnConfig, idx) => {
      const field = columnConfig;

      columns.push({
        field: field,
        fieldName: field.name,
        fieldIndex: idx,
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
            const rawValue = rowData[column.fieldName];
            return (
              <div className="sample-cell w-100 text-nowrap">
                {renderTableCellContent(rawValue)}
              </div>
            );
          } else {
            return cellData;
          }
        },
        createPipelineStatus: this.props.createPipelineStatus,
        dataGetter: ({ rowData, rowIndex }) =>
          field.id !== 0 ? rowData[field.name] : rowIndex + 1,
        flexGrow: field.id,
        frozen: field.id === 0 ? "left" : false,
        handleFieldChangeFunc: this.props.handleFieldChangeFunc,
        isRowNumber: field.id === 0,
        key: parseInt(field.id),
        resizable: true,
        title: field.name,
        width: field.id === 0 ? firstColumnWidth : defaultColumnWidth,
      });
    });

    return columns;
  }

  componentDidMount() {
    this.props.fetchStream(this.getStreamId());
    this.props.inspectStream(this.getStreamId(), this.state.inspectLimit);
  }

  updateInspectLimit(event) {
    const limit = isNaN(parseInt(event.target.value))
      ? 100
      : parseInt(event.target.value);
    this.setState({ inspectLimit: limit });
    this.props.inspectStream(this.getStreamId(), limit);
  }

  updateShowApiCall(showApiCall) {
    this.setState({
      showApiCall: showApiCall,
    });
  }

  toggleShowGrid(event) {
    event.preventDefault();

    this.setState({
      showGrid: !this.state.showGrid,
    });
  }

  getStreamId() {
    return this.props.match.params.id;
  }

  render() {
    if (![undefined, ""].includes(this.props.streams.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.streams.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    const stream = this.props.streams.stream;

    const loader = (
      <div className="row">
        <div className="col-12 justify-content-center d-flex pt-5 mt-5">
          <button
            className="btn btn-primary d-flex align-items-center"
            type="button"
            disabled
          >
            <span
              className="spinner-border spinner-border-sm me-2"
              role="status"
              aria-hidden="true"
            ></span>
            Loading...
          </button>
        </div>
      </div>
    );

    if (stream === undefined) {
      return <div className="container">{loader}</div>;
    }

    const header = (
      <div className="row">
        <Breadcrumb
          items={[
            { name: "Streams", uri: "/streams" },
            { name: stream.uuid, uri: `/streams/${stream.uuid}` },
            { name: "Inspect" },
          ]}
        />
        <Header
          apiDocs="https://docs.datacater.io/docs/api/streams/"
          apiPath={`/streams/${stream.uuid}/inspect?limit=${this.state.inspectLimit}`}
          title={stream.name || "Untitled stream"}
          updateCallback={this.updateShowApiCall}
        />
      </div>
    );

    if (this.props.streams.inspectingStream) {
      return (
        <div className="container">
          {header}
          {loader}
        </div>
      );
    }

    const sampleRecords = this.props.streams.inspectionResult;
    const profile = profileRecords(sampleRecords);

    const records = sampleRecords.map(function (sample, index) {
      return Object.assign({}, sample["value"], { id: index + 1 });
    });

    // Adjust height of grid if API call is shown,
    // which leaves less vertical space for the grid
    const gridClassNames = this.state.showApiCall
      ? "col-12 w-100 px-0 datacater-grid-container datacater-grid-container-api-call"
      : "col-12 w-100 px-0 datacater-grid-container";

    const gridButtonClassNames = this.state.showGrid
      ? "btn btn-sm btn-primary text-white"
      : "btn btn-sm btn-outline-primary";

    const rawButtonClassNames = !this.state.showGrid
      ? "btn btn-sm btn-primary text-white"
      : "btn btn-sm btn-outline-primary";

    return (
      <>
        <div className="container">
          {header}
          <div className="row g-3 align-items-center mt-2 justify-content-end">
            <div className="col-auto">
              <div className="btn-group">
                <a
                  href={`/streams/${stream.uuid}/inspect`}
                  onClick={this.toggleShowGrid}
                  className={gridButtonClassNames}
                >
                  <Table className="feather-icon" /> Grid
                </a>
                <a
                  href={`/streams/${stream.uuid}/inspect`}
                  onClick={this.toggleShowGrid}
                  className={rawButtonClassNames}
                >
                  <Code className="feather-icon" /> Raw
                </a>
              </div>
            </div>
            <div className="col-auto me-3">{sampleRecords.length} records</div>
            <div className="col-auto">
              <label className="col-form-label">Limit:</label>
            </div>
            <div className="col-auto">
              <input
                type="text"
                className="form-control form-control-sm"
                onChange={this.updateInspectLimit}
                aria-describedby="passwordHelpInline"
                placeholder="100"
                value={this.state.inspectLimit}
                style={{ width: "75px" }}
              />
            </div>
          </div>
          {sampleRecords.length === 0 && (
            <div className="alert alert-warning mt-4" role="alert">
              We couldn&apos;t extract any sample records. Is your stream empty?
            </div>
          )}
        </div>
        {sampleRecords.length > 0 && this.state.showGrid && (
          <div className="container-fluid datacater-grid-stream-inspect">
            <div className="row mt-2">
              <div className={gridClassNames} style={{ position: "relative" }}>
                <AutoResizer>
                  {({ width, height }) => (
                    <BaseTable
                      classPrefix="datacater-grid"
                      columns={this.getColumns(profile)}
                      components={{ TableHeaderCell }}
                      data={records}
                      fixed
                      headerHeight={153}
                      height={height}
                      rowHeight={24}
                      width={width}
                    />
                  )}
                </AutoResizer>
              </div>
            </div>
          </div>
        )}
        {sampleRecords.length > 0 && !this.state.showGrid && (
          <div className="container">
            <div className="row mt-4">
              <div className="col-12">
                <div className="card">
                  <div className="card-body">
                    <pre>
                      {JSON.stringify(
                        this.props.streams.inspectionResult,
                        null,
                        2
                      )}
                    </pre>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    streams: state.streams,
  };
};

const mapDispatchToProps = {
  fetchStream: fetchStream,
  inspectStream: inspectStream,
};

export default connect(mapStateToProps, mapDispatchToProps)(InspectStream);

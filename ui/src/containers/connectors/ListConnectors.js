import React, { Component } from "react";
import { connect } from "react-redux";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import TimeAgo from "javascript-time-ago";
import en from "javascript-time-ago/locale/en";
import { fetchConnectors } from "../../actions/connectors";

class ListConnectors extends Component {
  componentDidMount() {
    this.props.fetchConnectors();
  }

  render() {
    if (![undefined, ""].includes(this.props.connectors.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.connectors.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    const connectors = this.props.connectors.connectors.sort(
      (a, b) => Date.parse(b.updatedAt) - Date.parse(a.updatedAt)
    );

    TimeAgo.addDefaultLocale(en);
    const timeAgo = new TimeAgo("en-US");

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb items={[{ name: "Connectors" }]} />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/connectors/"
            apiPath="/connectors/"
            title="Connectors"
            subTitle="Connectors integrate Streams with external systems."
          />
        </div>
        <div className="row mt-4">
          <div className="col-12">
            {connectors.length === 0 && (
              <div className="card">
                <div className="card-body">
                  <div className="d-flex align-items-center justify-content-center m-5">
                    No connectors available.
                  </div>
                </div>
              </div>
            )}
            {connectors.length > 0 && (
              <div className="list-group">
                {connectors.map((connector) => (
                  <a
                    href={`/connectors/${connector.uuid}`}
                    key={connector.uuid}
                    className="list-group-item list-group-item-action bg-white p-4"
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h5 className="d-flex align-items-center">
                        {connector.name || "Untitled connector"}
                      </h5>
                      <small className="d-flex align-items-center">
                        {connector.uuid}
                      </small>
                    </div>
                    {connector.updatedAt !== undefined &&
                      !isNaN(Date.parse(connector.updatedAt)) && (
                        <div className="d-flex w-100 justify-content-end">
                          <small className="text-muted">
                            Last modified:{" "}
                            {timeAgo.format(new Date(connector.updatedAt))}
                          </small>
                        </div>
                      )}
                  </a>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    connectors: state.connectors,
  };
};

const mapDispatchToProps = {
  fetchConnectors: fetchConnectors,
};

export default connect(mapStateToProps, mapDispatchToProps)(ListConnectors);

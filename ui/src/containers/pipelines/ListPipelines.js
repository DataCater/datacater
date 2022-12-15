import React, { Component } from "react";
import { connect } from "react-redux";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import TimeAgo from "javascript-time-ago";
import en from "javascript-time-ago/locale/en";
import { fetchPipelines } from "../../actions/pipelines";

class ListPipelines extends Component {
  componentDidMount() {
    this.props.fetchPipelines();
  }

  render() {
    if (![undefined, ""].includes(this.props.pipelines.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.pipelines.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    const pipelines = this.props.pipelines.pipelines.sort(
      (a, b) => Date.parse(b.updatedAt) - Date.parse(a.updatedAt)
    );

    TimeAgo.addDefaultLocale(en);
    const timeAgo = new TimeAgo("en-US");

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb items={[{ name: "Pipelines" }]} />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/pipelines/"
            apiPath="/pipelines/"
            buttons={
              <>
                {pipelines.length > 0 && (
                  <a
                    href="/pipelines/new"
                    className="btn btn-primary text-white ms-2"
                  >
                    Create new pipeline
                  </a>
                )}
              </>
            }
            title="Pipelines"
            subTitle="Pipelines stream records between your Streams and can apply filters and transforms on the way."
          />
        </div>
        <div className="row mt-4">
          <div className="col-12">
            {pipelines.length === 0 && (
              <div className="card">
                <div className="card-body">
                  <div className="d-flex align-items-center justify-content-center m-5">
                    <a
                      href="/pipelines/new"
                      className="btn btn-lg text-white"
                      style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
                    >
                      Create your first pipeline
                    </a>
                  </div>
                </div>
              </div>
            )}
            {pipelines.length > 0 && (
              <div className="list-group">
                {pipelines.map((pipeline, idx) => (
                  <a
                    href={`/pipelines/${pipeline.uuid}`}
                    key={idx}
                    className="list-group-item list-group-item-action bg-white p-4"
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h5 className="d-flex align-items-center">
                        {pipeline.name || "Untitled pipeline"}
                      </h5>
                      <small className="d-flex align-items-center">
                        {pipeline.uuid}
                      </small>
                    </div>
                    {pipeline.updatedAt !== undefined &&
                      !isNaN(Date.parse(pipeline.updatedAt)) && (
                        <div className="d-flex w-100 justify-content-between mb-1">
                          <small className="d-flex align-items-center text-muted">
                            Last modified:{" "}
                            {timeAgo.format(new Date(pipeline.updatedAt))}
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
    pipelines: state.pipelines,
  };
};

const mapDispatchToProps = {
  fetchPipelines: fetchPipelines,
};

export default connect(mapStateToProps, mapDispatchToProps)(ListPipelines);

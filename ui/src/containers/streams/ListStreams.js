import React, { Component } from "react";
import { connect } from "react-redux";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import TimeAgo from "javascript-time-ago";
import en from "javascript-time-ago/locale/en";
import { fetchStreams } from "../../actions/streams";

class ListStreams extends Component {
  componentDidMount() {
    this.props.fetchStreams();
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

    const streams = this.props.streams.streams.sort(
      (a, b) => Date.parse(b.updatedAt) - Date.parse(a.updatedAt)
    );

    TimeAgo.addDefaultLocale(en);
    const timeAgo = new TimeAgo("en-US");

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb items={[{ name: "Streams" }]} />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/streams/"
            apiPath="/streams/"
            buttons={
              <>
                {streams.length > 0 && (
                  <a
                    href="/streams/new"
                    className="btn btn-primary text-white ms-2"
                  >
                    Create new stream
                  </a>
                )}
              </>
            }
            title="Streams"
            subTitle="Streams connect Apache Kafka® topics with your pipeline."
          />
        </div>
        <div className="row mt-4">
          <div className="col-12">
            {streams.length === 0 && (
              <div className="card">
                <div className="card-body">
                  <div className="d-flex align-items-center justify-content-center m-5">
                    <a
                      href="/streams/new"
                      className="btn btn-lg text-white"
                      style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
                    >
                      Create your first stream
                    </a>
                  </div>
                </div>
              </div>
            )}
            {streams.length > 0 && (
              <div className="list-group">
                {streams.map((stream) => (
                  <a
                    href={`/streams/${stream.uuid}`}
                    key={stream.uuid}
                    className="list-group-item list-group-item-action bg-white p-4"
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h5 className="d-flex align-items-center">
                        {stream.name}
                      </h5>
                      <small className="d-flex align-items-center">
                        {stream.uuid}
                      </small>
                    </div>
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <small className="d-flex-align-items-center">
                        {stream.spec.kafka["bootstrap.servers"]}
                      </small>
                      {stream.updatedAt !== undefined &&
                        !isNaN(Date.parse(stream.updatedAt)) && (
                          <small className="d-flex align-items-center text-muted">
                            Last modified:{" "}
                            {timeAgo.format(new Date(stream.updatedAt))}
                          </small>
                        )}
                    </div>
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
    streams: state.streams,
  };
};

const mapDispatchToProps = {
  fetchStreams: fetchStreams,
};

export default connect(mapStateToProps, mapDispatchToProps)(ListStreams);

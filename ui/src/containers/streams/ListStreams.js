import React, { Component } from "react";
import { connect } from "react-redux";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { fetchStreams } from "../../actions/streams";

class ListStreams extends Component {
  componentDidMount() {
    this.props.fetchStreams();
  }

  render() {
    const streams = this.props.streams.streams;

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
                    <small>{stream.spec.kafka["bootstrap.servers"]}</small>
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

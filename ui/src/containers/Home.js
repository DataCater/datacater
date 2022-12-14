import React, { Component } from "react";
import { connect } from "react-redux";
import { GitHub, Slack } from "react-feather";
import { fetchStreams } from "../actions/streams";
import { fetchPipelines } from "../actions/pipelines";
import "../scss/cards.scss";

class Home extends Component {
  constructor(props) {
    super(props);

    this.state = {
      loadedPipelines: false,
    };

    this.hideWelcomeWidget = this.hideWelcomeWidget.bind(this);
  }

  componentDidMount() {
    this.props.fetchStreams();
    this.props.fetchPipelines();
  }

  hideWelcomeWidget(event) {
    event.preventDefault();
    localStorage.setItem("showWelcomeScreen", false);
    // force re-rendering of component (without welcome screen)
    this.forceUpdate();
  }

  render() {
    const streamsAreMissing = this.props.streams.streams.length === 0;
    const pipelinesAreMissing = this.props.pipelines.pipelines.length === 0;

    return (
      <div className="container-fluid">
        <div className="row">
          <main role="main" className="col-10 mx-auto pt-3 px-4">
            <div className="row">
              <div className="col-12">
                <div className="card welcome-card mt-2 bg-gradient-purple">
                  <div className="card-body text-center">
                    <div className="row justify-content-center">
                      <div className="col-12">
                        <h2 className="fw-bold">Welcome to DataCater</h2>
                        <p className="text-white mb-3">
                          Get started using DataCater by creating your first
                          streams and pipelines or having a look at our
                          documentation.
                        </p>
                        <p className="mb-0">
                          {streamsAreMissing && (
                            <a
                              href="/streams/new/"
                              className="btn btn-primary lift text-white"
                            >
                              Create first stream
                            </a>
                          )}
                          {!streamsAreMissing && pipelinesAreMissing && (
                            <a
                              href="/pipelines/new/"
                              className="btn btn-primary lift text-white"
                            >
                              Create first pipeline
                            </a>
                          )}
                          <a
                            href="https://docs.datacater.io"
                            className="btn btn-light lift border ms-2"
                            target="_blank"
                            rel="noreferrer"
                          >
                            Docs
                          </a>
                          <a
                            href="https://join.slack.com/t/datacater/shared_invite/zt-17cga6jg3-rGdgQZU6iX~mJGC8j~UNlw"
                            className="btn btn-light lift border ms-2"
                            target="_blank"
                            rel="noreferrer"
                          >
                            <Slack className="feather-icon" />
                          </a>
                          <a
                            href="https://github.com/DataCater/datacater"
                            className="btn btn-light lift border ms-2"
                            target="_blank"
                            rel="noreferrer"
                          >
                            <GitHub className="feather-icon" />
                          </a>
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </main>
        </div>
      </div>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    pipelines: state.pipelines,
    streams: state.streams,
  };
};

const mapDispatchToProps = {
  fetchPipelines: fetchPipelines,
  fetchStreams: fetchStreams,
};

export default connect(mapStateToProps, mapDispatchToProps)(Home);

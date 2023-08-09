import React, { Component } from "react";
import { connect } from "react-redux";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { fetchProjectContext } from "../../actions/projectsContext";
import TimeAgo from "javascript-time-ago";
import en from "javascript-time-ago/locale/en";
import { fetchConfigs } from "../../actions/configs";

class ListConfigs extends Component {
  componentDidMount() {
    this.props.fetchConfigs();
  }

  componentWillReceiveProps(props) {
    if (props.projectsContext.project !== this.props.projectsContext.project) {
      this.props.fetchStreams();
    }
  }

  render() {
    if (![undefined, ""].includes(this.props.configs.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.configs.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    const configs = this.props.configs.configs.sort(
      (a, b) => Date.parse(b.updatedAt) - Date.parse(a.updatedAt)
    );

    TimeAgo.addDefaultLocale(en);
    const timeAgo = new TimeAgo("en-US");

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb items={[{ name: "Configs" }]} />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/configs/"
            apiPath="/configs/"
            buttons={
              <>
                {configs.length > 0 && (
                  <a
                    href="/configs/new"
                    className="btn btn-primary text-white ms-2"
                  >
                    Create new config
                  </a>
                )}
              </>
            }
            title="Configs"
            subTitle="Configs outsource the configuration of Streams and other resources."
          />
        </div>
        <div className="row mt-4">
          <div className="col-12">
            {configs.length === 0 && (
              <div className="card">
                <div className="card-body">
                  <div className="d-flex align-items-center justify-content-center m-5">
                    <a
                      href="/configs/new"
                      className="btn btn-lg text-white"
                      style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
                    >
                      Create your first config
                    </a>
                  </div>
                </div>
              </div>
            )}
            {configs.length > 0 && (
              <div className="list-group">
                {configs.map((config) => (
                  <a
                    href={`/configs/${config.uuid}`}
                    key={config.uuid}
                    className="list-group-item list-group-item-action bg-white p-4"
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h5 className="d-flex align-items-center">
                        {config.name || "Untitled config"}
                      </h5>
                      <small className="d-flex align-items-center">
                        {config.uuid}
                      </small>
                    </div>
                    {config.updatedAt !== undefined &&
                      !isNaN(Date.parse(config.updatedAt)) && (
                        <div className="d-flex w-100 justify-content-end">
                          <small className="text-muted">
                            Last modified:{" "}
                            {timeAgo.format(new Date(config.updatedAt))}
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
    configs: state.configs,
    projectsContext: state.projectsContext,
  };
};

const mapDispatchToProps = {
  fetchConfigs: fetchConfigs,
  fetchProjectContext: fetchProjectContext,
};

export default connect(mapStateToProps, mapDispatchToProps)(ListConfigs);

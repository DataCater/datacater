import React, { Component } from "react";
import { connect } from "react-redux";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { fetchProjectContext } from "../../actions/projectsContext";
import TimeAgo from "javascript-time-ago";
import en from "javascript-time-ago/locale/en";
import { fetchDeployments } from "../../actions/deployments";

class ListDeployments extends Component {
  componentDidMount() {
    this.props.fetchDeployments();
  }

  componentWillReceiveProps(props) {
    if (props.projectsContext.project !== this.props.projectsContext.project) {
      this.props.fetchDeployments();
    }
  }

  render() {
    if (![undefined, ""].includes(this.props.deployments.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.deployments.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    const deployments = this.props.deployments.deployments.sort(
      (a, b) => Date.parse(b.updatedAt) - Date.parse(a.updatedAt)
    );

    TimeAgo.addDefaultLocale(en);
    const timeAgo = new TimeAgo("en-US");

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb items={[{ name: "Deployments" }]} />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/deployments/"
            apiPath="/deployments/"
            buttons={
              <>
                {deployments.length > 0 && (
                  <a
                    href="/deployments/new"
                    className="btn btn-primary text-white ms-2"
                  >
                    Create new deployment
                  </a>
                )}
              </>
            }
            title="Deployments"
            subTitle="Deployments operate Pipelines."
          />
        </div>
        <div className="row mt-4">
          <div className="col-12">
            {deployments.length === 0 && (
              <div className="card">
                <div className="card-body">
                  <div className="d-flex align-items-center justify-content-center m-5">
                    <a
                      href="/deployments/new"
                      className="btn btn-lg text-white"
                      style={{ backgroundImage: "url(/images/bg-card.jpg)" }}
                    >
                      Create your first deployment
                    </a>
                  </div>
                </div>
              </div>
            )}
            {deployments.length > 0 && (
              <div className="list-group">
                {deployments.map((deployment) => (
                  <a
                    href={`/deployments/${deployment.uuid}`}
                    key={deployment.uuid}
                    className="list-group-item list-group-item-action bg-white p-4"
                  >
                    <div className="d-flex w-100 justify-content-between mb-1">
                      <h5 className="d-flex align-items-center">
                        {deployment.name || "Untitled deployment"}
                      </h5>
                      <small className="d-flex align-items-center">
                        {deployment.uuid}
                      </small>
                    </div>
                    {deployment.updatedAt !== undefined &&
                      !isNaN(Date.parse(deployment.updatedAt)) && (
                        <div className="d-flex w-100 justify-content-end">
                          <small className="text-muted">
                            Last modified:{" "}
                            {timeAgo.format(new Date(deployment.updatedAt))}
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
    deployments: state.deployments,
    projectsContext: state.projectsContext,
  };
};

const mapDispatchToProps = {
  fetchDeployments: fetchDeployments,
  fetchProjectContext: fetchProjectContext,
};

export default connect(mapStateToProps, mapDispatchToProps)(ListDeployments);

import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { fetchDeployment, updateDeployment } from "../../actions/configs";
import "../../scss/fonts.scss";

class EditConfig extends Component {
  constructor(props) {
    super(props);

    this.state = {
      updatingConfigFailed: false,
      errorMessages: {},
      config: undefined,
      configUpdated: false,
    };

    this.handleUpdateConfig = this.handleUpdateConfig.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  componentDidMount() {
    this.props
      .fetchConfig(this.getConfigId())
      .then(() =>
        this.setState({ config: this.props.config.spec })
      );
  }

  getConfigId() {
    return this.props.match.params.id;
  }

  handleUpdateConfig(event) {
    event.preventDefault();

    this.props
      .updateConfig(this.getConfigId(), this.state.config)
      .then(() => {
        if (this.props.configs.errorMessage !== undefined) {
          this.setState({
            configUpdated: false,
            errorMessage: this.props.configs.errorMessage,
          });
        } else {
          this.setState({
            configUpdated: true,
            errorMessage: "",
          });
        }
      });
  }

  handleChange(name, value, prefix) {
    let config = this.state.config;

    if (prefix === "metadata") {
      config.metadata[name] = value;
    } else if (prefix === "spec") {
      config.spec[name] = value;
    } else {
      config[name] = value;
    }

    this.setState({
      updatingConfigFailed: false,
      errorMessage: "",
      config: config,
    });
  }

  render() {
    if (this.state.configUpdated) {
      return <Redirect to={`/configs/${this.getDeploymentId()}`} />;
    }

    const config = this.state.config;

    if (config === undefined) {
      return <></>;
    }

    const apiPayload = Object.assign({}, config);
    delete apiPayload.uuid;
    delete apiPayload.createdAt;
    delete apiPayload.updatedAt;
    delete apiPayload.status;

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Configs", uri: "/configs" },
              { name: config.uuid, uri: `/configs/${config.uuid}` },
              { name: "Edit" },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/configs/"
            apiPath={`/configs/${config.uuid}`}
            httpMethod="PUT"
            requestBody={apiPayload}
            title={config.name || "Untitled config"}
          />
          <form>
            <div className="col-12 mt-4">
              <label htmlFor="name" className="form-label">
                Name{" "}
              </label>
              <input
                type="text"
                className="form-control"
                id="name"
                name="name"
                onChange={(event) => {
                  this.handleChange("name", event.target.value);
                }}
                value={this.state.config["name"] || ""}
              />
            </div>
            {![undefined, ""].includes(this.state.errorMessage) && (
              <div className="alert alert-danger mt-4">
                <p className="h6 fs-bolder">API response:</p>
                {this.state.errorMessage}
              </div>
            )}
            <div className="col-12 mt-4">
              <a
                href={`/configs/${config.uuid}`}
                className="btn btn-primary text-white mb-4"
                onClick={this.handleUpdateConfig}
              >
                Update config
              </a>
            </div>
          </form>
        </div>
      </div>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    configs: state.configs,
  };
};

const mapDispatchToProps = {
  fetchConfig: fetchConfig,
  updateConfig: updateConfig,
};

export default connect(mapStateToProps, mapDispatchToProps)(EditConfig);

import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { addConfig } from "../../actions/configs";
import "../../scss/fonts.scss";

class NewConfig extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingConfigFailed: false,
      errorMessages: {},
      config: {
        spec: {},
      },
      configCreated: false,
    };

    this.handleCreateConfig = this.handleCreateConfig.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  handleCreateConfig(event) {
    event.preventDefault();

    this.props.addConfig(this.state.config).then(() => {
      if (this.props.configs.errorMessage !== undefined) {
        this.setState({
          configCreated: false,
          errorMessage: this.props.configs.errorMessage,
        });
      } else {
        this.setState({
          configCreated: true,
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
      creatingConfigFailed: false,
      errorMessage: "",
      config: config,
    });
  }

  render() {
    if (this.state.configCreated) {
      return (
        <Redirect
          to={"/configs/" + this.props.configs.config.uuid}
        />
      );
    }

    const config = this.state.config;

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Configs", uri: "/configs" },
              { name: "New config" },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/configs/"
            apiPath="/configs/"
            httpMethod="POST"
            requestBody={this.state.config}
            title="Create new config"
            subTitle="Configs operate Pipelines."
          />
          <form>
            <div className="col-12 mt-4">
              <label htmlFor="name" className="form-label">
                Name
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
                href="/configs/new"
                className="btn btn-primary text-white mb-4"
                onClick={this.handleCreateConfig}
              >
                Create config
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
  addonfig: addConfig,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewConfig);

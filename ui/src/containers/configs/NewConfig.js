import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import Creatable from "react-select/creatable";
import Select from "react-select";
import Breadcrumb from "../../components/layout/Breadcrumb";
import { getConfigKindOptions } from "../../helpers/getConfigKindOptions";
import Header from "../../components/layout/Header";
import { addConfig } from "../../actions/configs";
import { fetchPipelines } from "../../actions/pipelines";
import "../../scss/fonts.scss";

class NewConfig extends Component {
  constructor(props) {
    super(props);

    this.state = {
      creatingConfigFailed: false,
      errorMessages: {},
      config: {
        name: "",
        kind: "STREAM",
        metadata: {
            labels: {}
        },
        spec: {},
      },
      tempLabel: {
        labelKey: "",
        labelValue: "",
      },
      deployment: {
        spec: {},
      },
      stream: {
        spec: {
          kind: "KAFKA",
          kafka: {
            topic: {
              config: {},
            },
          },
        },
      },
      configCreated: false,
    };

    this.handleCreateConfig = this.handleCreateConfig.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.addLabel = this.addLabel.bind(this);
    this.removeLabel = this.removeLabel.bind(this);
  }


  componentDidMount() {
    this.props.fetchPipelines();
  }

    updateTempLabel(field, value) {
      let tempLabel = this.state.tempLabel;
      tempLabel[field] = value;
      this.setState({ tempLabel: tempLabel });
    }

    addLabel(event) {
        event.preventDefault();
        const tempLabel = this.state.tempLabel;
        let config = this.state.config;
        config.metadata.labels[tempLabel.labelKey] = tempLabel.labelValue;
        this.setState({ config: config, tempLabel: tempLabel });
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
    let deployment = this.state.deployment;
    let stream = this.state.stream;

    if(prefix === "deployment.spec"){
        deployment.spec[name] = value;
    } else{
        config[name] = value;
    }

    if(prefix.includes("deployment")){
        config.spec = deployment.spec;
    } else{
        config.spec = stream.spec;
    }

    this.setState({
      creatingConfigFailed: false,
      errorMessage: "",
      config: config,
    });
  }

    removeLabel(event) {
      event.preventDefault();
      let config = this.state.config;
      const label = event.target.dataset.label;
      delete config.metadata.labels[label];
      this.setState({ config: config });
    }

    updateKindOption(field, value) {
      let config = this.state.config;

      config.kind = value;

      this.setState({ config: config });
    }

        setDefaultKind(field, value) {
          let config = this.state.config;

          config.kind = value;

          this.setState({ config: config });
          return this.state.config.kind;
        }

  render() {
    if (this.state.configCreated) {
      return (
        <Redirect
          to={"/configs/" + this.props.configs.config.uuid}
        />
      );
    }

    const kindOptions = getConfigKindOptions();
    const config = this.state.config;
    const defaultKind = "STREAM";
    const addedLabels = Object.keys(config.metadata.labels);

    const pipelineOptions = this.props.pipelines.pipelines.map((pipeline) => {
          const name = `${pipeline.name || "Untitled pipeline"} (${pipeline.uuid})`;
          return { value: pipeline.uuid, label: name };
        });

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
            subTitle="Configs are used to outsource the configuration of streams and deployments."
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
            <div className="col-12 mt-2">
              <label htmlFor="kind" className="form-label">
                kind
              </label>
              <Creatable
                defaultValue={kindOptions.find(
                  (kind) => kind.value === defaultKind
                )}
                isSearchable
                options={kindOptions}
                onChange={(kind) => {
                  this.updateKindOption("kind", kind.value);
                }}
              />
            </div>
            {addedLabels.map((labels) => (
              <div className="col-12 mt-2" key={labels}>
                <label htmlFor={labels} className="form-label">
                  {labels}
                  <a
                    className="ms-2 fs-7"
                    data-label={labels}
                    data-prefix="metadata.labels"
                    href="/config/new"
                    onClick={this.removeLabel}
                  >
                    Remove
                  </a>
                </label>
                <input
                  type="text"
                  className="form-control"
                  id={labels}
                  data-prefix="metadata.labels"
                  name={labels}
                  onChange={this.handleChange}
                  value={this.state.config.metadata.labels[labels] || ""}
                />
              </div>
            ))}
            <div className="col-12 mt-3">
              <h6 className="d-inline me-2">Add labels</h6>
              <span className="text-muted fs-7">
              used for matching the config to streams or deployments.
              </span>
            </div>
            <div className="col-12 mt-2">
              <div className="row">
                <div className="col-md-3">
                  <label className="form-label">Key</label>
                  <input
                    type="text"
                    className="form-control"
                    name="labelKey"
                    onChange={(event) => {
                      this.updateTempLabel(
                        "labelKey",
                        event.target.value
                      );
                    }}
                    value={this.state.tempLabel.labelKey || ""}
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Value</label>
                  <input
                    type="text"
                    className="form-control"
                    name="labelValue"
                    onChange={(event) => {
                      this.updateTempLabel(
                        "labelValue",
                        event.target.value
                      );
                    }}
                    value={this.state.tempLabel.labelValue || ""}
                  />
                </div>
                <div className="col-md-3 d-flex align-items-end">
                  <a
                    href="/configs/new"
                    className="btn btn-outline-primary"
                    data-prefix="metadata.labels"
                    onClick={this.addLabel}
                  >
                    Add
                  </a>
                </div>
              </div>
            </div>



            {[undefined, defaultKind].includes(this.state.config["kind"]) && (
              <>
                <div className="col-12 mt-4">

                </div>
              </>
            )}



            {this.state.config["kind"] == "DEPLOYMENT" && (
              <>
                <div className="col-12 mt-4">
                  <label className="form-label">Pipeline</label>
                  <Select
                    isSearchable
                    isClearable
                    options={pipelineOptions}
                    onChange={(value) => {
                      this.handleChange("pipeline", value.value, "deployment.spec");
                    }}
                  />
                </div>
              </>
            )}
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
    pipelines: state.pipelines,
  };
};

const mapDispatchToProps = {
  addonfig: addConfig,
  fetchPipelines: fetchPipelines,
};

export default connect(mapStateToProps, mapDispatchToProps)(NewConfig);

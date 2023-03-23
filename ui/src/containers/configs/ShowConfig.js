import React, { Component } from "react";
import { connect } from "react-redux";
import { Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import { deleteConfig, fetchConfig } from "../../actions/configs";
import { jsonToYaml } from "../../helpers/jsonToYaml";

class ShowConfig extends Component {
  constructor(props) {
    super(props);
    this.state = {
      configDeleted: false,
    };
    this.handleDelete = this.handleDelete.bind(this);
  }

  componentDidMount() {
    this.props.fetchConfig(this.getConfigId());
  }

  getConfigId() {
    return this.props.match.params.id;
  }

  handleDelete(event) {
    event.preventDefault();

    if (window.confirm("Are you sure that you want to delete the config?")) {
      this.props.deleteConfig(this.getConfigId()).then(() => {
        this.setState({ configDeleted: true });
      });
    }
  }

  render() {
    if (this.state.configDeleted) {
      return <Redirect to="/configs" />;
    }

    const config = this.props.configs.config;

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

    if (config === undefined) {
      return <div></div>;
    }

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Configs", uri: "/configs" },
              { name: config.uuid },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/configs/"
            apiPath={`/configs/${config.uuid}`}
            buttons={
              <>
                <a
                  href={`/configs/${config.uuid}/edit`}
                  className="btn btn-primary text-white ms-2"
                >
                  Edit
                </a>
                <a
                  href={`/configs/${config.uuid}`}
                  onClick={this.handleDelete}
                  className="btn btn-light btn-outline-danger ms-2"
                >
                  <Trash2 className="feather-icon" />
                </a>
              </>
            }
            title={config.name || "Untitled config"}
          />
        </div>
        <div className="row mt-4">
          <div className="col-12">
            <div className="card">
              <div className="card-body">
                <SyntaxHighlighter
                  language="yaml"
                  showLineNumbers={true}
                  showInlineLineNumbers={true}
                  customStyle={{ marginBottom: "0px", background: "none" }}
                >
                  {jsonToYaml(config)}
                </SyntaxHighlighter>
              </div>
            </div>
          </div>
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
  deleteConfig: deleteConfig,
  fetchConfig: fetchConfig,
};

export default connect(mapStateToProps, mapDispatchToProps)(ShowConfig);

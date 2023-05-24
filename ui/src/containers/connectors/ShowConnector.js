import React, { Component } from "react";
import { connect } from "react-redux";
import { Trash2 } from "react-feather";
import { Redirect } from "react-router-dom";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import Breadcrumb from "../../components/layout/Breadcrumb";
import Header from "../../components/layout/Header";
import ApiCall from "../../components/layout/ApiCall";
import { deleteConnector, fetchConnector } from "../../actions/connectors";
import { jsonToYaml } from "../../helpers/jsonToYaml";

class ShowConnector extends Component {
  constructor(props) {
    super(props);
    this.state = {
      connectorDeleted: false,
    };
    this.handleDelete = this.handleDelete.bind(this);
  }

  componentDidMount() {
    this.props.fetchConnector(this.getConnectorId());
  }

  getConnectorId() {
    return this.props.match.params.id;
  }

  handleDelete(event) {
    event.preventDefault();

    if (window.confirm("Are you sure that you want to delete the connector?")) {
      this.props.deleteConnector(this.getConnectorId()).then(() => {
        this.setState({ connectorDeleted: true });
      });
    }
  }

  render() {
    if (this.state.connectorDeleted) {
      return <Redirect to="/connectors" />;
    }

    const connector = this.props.connectors.connector;

    if (![undefined, ""].includes(this.props.connectors.errorMessage)) {
      return (
        <div className="container">
          <div className="col-12 mt-4">
            <div className="alert alert-danger">
              <p className="h6 fs-bolder">API response:</p>
              {this.props.connectors.errorMessage}
            </div>
          </div>
        </div>
      );
    }

    if (connector === undefined) {
      return <div></div>;
    }

    return (
      <div className="container">
        <div className="row">
          <Breadcrumb
            items={[
              { name: "Connectors", uri: "/connectors" },
              { name: connector.uuid },
            ]}
          />
          <Header
            apiDocs="https://docs.datacater.io/docs/api/connectors/"
            apiPath={`/connectors/${connector.uuid}`}
            buttons={
              <a
                href={`/connectors/${connector.uuid}`}
                onClick={this.handleDelete}
                className="btn btn-light btn-outline-danger ms-2"
              >
                <Trash2 className="feather-icon" />
              </a>
            }
            title={connector.name || "Untitled connector"}
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
                  {jsonToYaml(connector)}
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
    connectors: state.connectors,
  };
};

const mapDispatchToProps = {
  deleteConnector: deleteConnector,
  fetchConnector: fetchConnector,
};

export default connect(mapStateToProps, mapDispatchToProps)(ShowConnector);

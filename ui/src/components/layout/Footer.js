import React, { Component } from "react";
import { Link, Redirect } from "react-router-dom";
import { connect } from "react-redux";
import { fetchInfo } from "../../actions/info";
import { Info, BookOpen, User } from "react-feather";
import "../../scss/nav.scss";

class Footer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showVersion: false,
      showDocumentation: false,
      showContact: false,
      errorMessage: "",
      errorMessages: {},
      info: {
        version: {},
        resources: {},
        contact: {},
      },
    };
    this.toggleFooterDropdown = this.toggleFooterDropdown.bind(this);
  }

  renderFooterNavDropdownSelector(label, actionItem, icon) {
    let classNames = "nav-item";
    return (
      <ul className={classNames}>
        <a
          className="nav-link text-muted"
          onClick={() => this.toggleFooterDropdown(actionItem)}
          role="button"
        >
          {icon}
          {label}
        </a>
      </ul>
    );
  }

  toggleFooterDropdown(item) {
    switch (item) {
      case "version":
        this.setState({
          showVersion: !this.state.showVersion,
          showDocumentation: false,
          showContact: false,
        });
        break;
      case "documentation":
        this.setState({
          showVersion: false,
          showDocumentation: !this.state.showDocumentation,
          showContact: false,
        });
        break;
      case "contact":
        this.setState({
          showVersion: false,
          showDocumentation: false,
          showContact: !this.state.showContact,
        });
        break;
      default:
        this.setState({
          showVersion: false,
          showDocumentation: false,
          showContact: false,
        });
        break;
    }
  }

  componentDidMount() {
    this.props
      .fetchInfo()
      .then(() => this.setState({ info: this.props.info.info }));
  }

  render() {
    const info = this.state.info;

    if (info == null) {
      return <></>;
    }

    return (
      <div>
        <nav
          id="bottomnav"
          className="navbar navbar-expand-lg navbar-light justify-content-center"
        >
          {this.renderFooterNavDropdownSelector(
            "Version",
            "version",
            <Info className="feather-icon me-2" />
          )}
          {this.renderFooterNavDropdownSelector(
            "Documentation",
            "documentation",
            <BookOpen className="feather-icon me-2" />
          )}
          {this.renderFooterNavDropdownSelector(
            "Contact",
            "contact",
            <User className="feather-icon me-2" />
          )}
        </nav>
        <div
          id="version"
          style={this.state.showVersion ? {} : { display: "none" }}
        >
          <ul className="list-inline text-center d-flex justify-content-center align-items-center text-muted">
            <li className="mx-3">Version: {info.version.version}</li>
            <li className="mx-3">Base Image: {info.version.baseImage}</li>
            <li className="mx-3">
              Pipeline Image: {info.version.pipelineImage}
            </li>
            <li className="mx-3">
              Python-Runner Image: {info.version.pythonRunnerImage}
            </li>
          </ul>
        </div>
        <div
          id="documentation"
          style={this.state.showDocumentation ? {} : { display: "none" }}
        >
          <ul className="list-inline text-center d-flex justify-content-center align-items-center text-muted">
            <li className="mx-3">
              <a href="{info.resources.streams.documentationUrl}">streams</a>
            </li>
            <li className="mx-3">
              <a href="{info.resources.deployments.documentationUrl}">
                deployments
              </a>
            </li>
            <li className="mx-3">
              <a href="{info.resources.pipelines.documentationUrl}">
                pipelines
              </a>
            </li>
            <li className="mx-3">
              <a href="{info.resources.configs.documentationUrl}">configs</a>
            </li>
          </ul>
        </div>
        <div
          id="contact"
          style={this.state.showContact ? {} : { display: "none" }}
        >
          <ul className="list-inline text-center d-flex justify-content-center align-items-center text-muted">
            <li className="mx-3">Name: {info.contact.name}</li>
            <li className="mx-3">E-Mail: {info.contact.email}</li>
            <li className="mx-3">URL: {info.contact.url}</li>
          </ul>
        </div>
      </div>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    info: state.info,
  };
};

const mapDispatchToProps = {
  fetchInfo: fetchInfo,
};

export default connect(mapStateToProps, mapDispatchToProps)(Footer);

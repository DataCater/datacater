import React, { Component } from "react";
import { Link, Redirect } from "react-router-dom";
import { connect } from "react-redux";
import { fetchInfo } from "../../actions/info";
import {
  Info,
  BookOpen,
  User,
} from "react-feather";
import "../../scss/nav.scss";


class Footer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: "",
      errorMessages: {},
      info: {
        version: {},
        resources: {},
        contact: {},
      },
    };
  }

    renderNavItem(label, href, icon) {
      let classNames = "nav-link d-flex align-items-center";
      classNames = "nav-item";
      if (
        window.location.pathname !== undefined &&
        window.location.pathname.includes(href)
      ) {
        classNames += " active";
      }

      return (
        <ul className={classNames}>
          <Link className="nav-link text-black" to={href} role="button">
            {icon}
            {label}
          </Link>
        </ul>
      );
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
          <nav
            id="bottomnav"
            className="navbar navbar-expand-lg navbar-light justify-content-center"
          >
          {this.renderNavItem(
            "Version",
            "/",
            <Info className="feather-icon me-2" />
          )}
          {this.renderNavItem(
            "Documentation",
            "/",
            <BookOpen className="feather-icon me-2" />
          )}
          {this.renderNavItem(
            "Contact",
            "/",
            <User className="feather-icon me-2" />
          )}
 </nav>
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
/*
       <div>
          <div>
              <p>version info:</p>
              <p>version: {info.version.version}</p>
              <p>base image: {info.version.baseImage}</p>
              <p>pipeline image: {info.version.pipelineImage}</p>
              <p>python-runner image: {info.version.pythonRunnerImage}</p>
          </div>
          <div>
              <p>documentation:</p>
              <p><a href="{info.resources.streams.documentationUrl}">streams</a></p>
              <p><a href="{info.resources.deployments.documentationUrl}">deployments</a></p>
              <p><a href="{info.resources.pipelines.documentationUrl}">pipelines</a></p>
              <p><a href="{info.resources.configs.documentationUrl}">configs</a></p>
          </div>
          <div>
                <p>contact:</p>
                <p>name: {info.contact.name}</p>
                <p>email: {info.contact.email}</p>
                <p>url: {info.contact.url}</p>
            </div>
      </div> */
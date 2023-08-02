import React, { Component } from "react";
import { Link, Redirect } from "react-router-dom";
import { signOut } from "../../actions/user_sessions";
import { fetchInfo } from "../../actions/info";
import { fetchProjects } from "../../actions/projects";
import { connect } from "react-redux";
import Dropdown from "react-bootstrap/Dropdown";
import DropdownButton from "react-bootstrap/DropdownButton";
import {
  Book,
  Code,
  Home,
  Mail,
  LogOut,
  PlayCircle,
  Wind,
  Tool,
  Info,
} from "react-feather";
import "../../scss/nav.scss";

class Navigation extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: "",
      errorMessages: {},
      projects: [{ name: "default" }],
    };
    this.handleSignOut = this.handleSignOut.bind(this);
    this.renderProjectDropdown = this.renderProjectDropdown.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
  }

  componentDidMount() {
    this.props.fetchInfo();
    this.props.fetchProjects().then(() => {
      const projects = this.props.projects;
      if (projects !== undefined) {
        const wrappedProjects = projects.projects;
        this.setState({
          projects: wrappedProjects,
        });
      }
    });
  }

  renderNavItem(label, href, icon) {
    let classNames = "nav-link d-flex align-items-center";
    classNames = "nav-item";
    if (
      this.props.pathname !== undefined &&
      this.props.pathname.includes(href)
    ) {
      classNames += " active";
    }

    return (
      <li className={classNames}>
        <Link className="nav-link text-black" to={href} role="button">
          {icon}
          {label}
        </Link>
      </li>
    );
  }
  renderProjectDropdown(label) {
    const projects = this.state.projects;
    return (
      <DropdownButton
        className="me-2"
        variant="Primary"
        id="dropdown-basic-button"
        title={label}
      >
        {projects !== undefined &&
          projects.length > 0 &&
          projects.map((project) => (
            <Dropdown.Item key={project.name}>
              <small>{project.name}</small>
            </Dropdown.Item>
          ))}
      </DropdownButton>
    );
  }

  handleSignOut(event) {
    event.preventDefault();
    signOut();
    this.setState({ signedOut: true });
  }

  render() {
    if (this.state.signedOut === true) {
      return <Redirect to="/sign_in" />;
    }

    const info = this.props.info.info;

    return (
      <nav
        id="topnav"
        className="navbar navbar-expand-lg navbar-light border-bottom"
      >
        <div className="container">
          <a className="navbar-brand" href="/">
            <img
              src="/images/logo.png"
              className="navbar-brand-img"
              alt="DataCater logo"
              style={{ maxHeight: "2rem" }}
            />
          </a>
          <button
            className="navbar-toggler"
            type="button"
            data-toggle="collapse"
            data-target="#navbarsExample07"
            aria-controls="navbarsExample07"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>

          <div className="collapse navbar-collapse" id="navbarsExample07">
            <ul className="navbar-nav me-auto">
              {this.renderNavItem(
                "Home",
                "/home",
                <Home className="feather-icon me-2" />
              )}
              {this.renderNavItem(
                "Streams",
                "/streams",
                <Wind className="feather-icon me-2" />
              )}
              {this.renderNavItem(
                "Pipelines",
                "/pipelines",
                <Code className="feather-icon me-2" />
              )}
              {this.renderNavItem(
                "Deployments",
                "/deployments",
                <PlayCircle className="feather-icon me-2" />
              )}
              {this.renderNavItem(
                "Configs",
                "/configs",
                <Tool className="feather-icon me-2" />
              )}
              {this.renderProjectDropdown("Project")}
            </ul>
            {info !== undefined && info.version !== undefined && (
              <DropdownButton
                className="me-2"
                variant="Primary"
                id="dropdown-basic-button"
                title={<Info className="feather-icon" />}
              >
                <Dropdown.Item disabled>
                  <small>Base Image: {info.version.baseImage}</small>
                </Dropdown.Item>
                <Dropdown.Item disabled>
                  <small>Pipeline Image: {info.version.pipelineImage}</small>
                </Dropdown.Item>
                <Dropdown.Item disabled>
                  <small>
                    Python-Runner Image: {info.version.pythonRunnerImage}
                  </small>
                </Dropdown.Item>
              </DropdownButton>
            )}
            <div className="my-2 my-md-0">
              <a
                href="/sign_out/"
                className="nav-link"
                onClick={this.handleSignOut}
              >
                <LogOut className="feather-icon" />{" "}
                <span className="ms-1">Sign out</span>
              </a>
            </div>
          </div>
        </div>
      </nav>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    info: state.info,
    projects: state.projects,
  };
};

const mapDispatchToProps = {
  fetchInfo: fetchInfo,
  fetchProjects: fetchProjects,
};

export default connect(mapStateToProps, mapDispatchToProps)(Navigation);

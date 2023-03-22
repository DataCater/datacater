import React, { Component } from "react";
import { Link, Redirect } from "react-router-dom";
import { signOut } from "../../actions/user_sessions";
import {
  Book,
  Code,
  Home,
  Mail,
  LogOut,
  PlayCircle,
  Wind,
  Tool,
} from "react-feather";
import "../../scss/nav.scss";

class Navigation extends Component {
  constructor(props) {
    super(props);
    this.state = {};
    this.handleSignOut = this.handleSignOut.bind(this);
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
      <li className={classNames}>
        <Link className="nav-link text-black" to={href} role="button">
          {icon}
          {label}
        </Link>
      </li>
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
            </ul>
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

export default Navigation;

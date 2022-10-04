import React, { Component } from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";
import { signIn } from "../actions/user_sessions";
import "../scss/sign-in.scss";

class Login extends Component {
  constructor(props) {
    super(props);
    this.state = {
      formErrors: "",
      formValues: {
        username: "",
        password: "",
      },
      loginSuccessful: false,
    };

    this.handleLogin = this.handleLogin.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  componentDidMount() {
    // do a small trick to add CSS classes to dom elements outside of
    // the application's root element
    const bodyDomElement = document.querySelector("body");
    const rootDomElement = document.querySelector("#root");
    bodyDomElement.className =
      "d-flex align-items-center border-top border-primary";
    rootDomElement.classList.add("container-fluid");
    rootDomElement.classList.add("pe-lg-0");

    document.title = "Sign In | DataCater";
  }

  componentWillUnmount() {
    // get rid of the added CSS classes
    const bodyDomElement = document.querySelector("body");
    const rootDomElement = document.querySelector("#root");
    bodyDomElement.className = "";
    rootDomElement.classList.remove("container-fluid");

    document.title = "DataCater";
  }

  handleLogin(event) {
    event.preventDefault();
    this.props
      .signIn(this.state.formValues.username, this.state.formValues.password)
      .then(() => {
        if (this.props.userSessions.userIsAuthenticated === true) {
          this.setState({
            formErrors: "",
            loginSuccessful: true,
          });
        } else {
          this.setState({
            formErrors: "Sign in failed.",
            loginSuccessful: false,
          });
        }
      });
  }

  handleChange(event) {
    let userForm = this.state.formValues;
    userForm[event.target.name] = event.target.value;
    this.setState({ formValues: userForm });
  }

  render() {
    if (this.state.loginSuccessful === true) {
      return <Redirect to="/" />;
    }

    return (
      <React.Fragment>
        <div className="row justify-content-center me-lg-0">
          <div className="col-12 col-md-5 col-lg-6 col-xl-4 px-lg-4 my-5 align-self-center">
            <a href="/">
              <img
                src="/images/logo.png"
                alt="DataCater Logo"
                className="login-logo"
              />
            </a>
            <h1 className="text-center mb-3" style={{ lineHeight: "125%" }}>
              Sign in
            </h1>
            {this.state.formErrors.length > 0 && (
              <div className="alert alert-danger mt-4" role="alert">
                {this.state.formErrors} Please reach out to our{" "}
                <a className="text-danger" href="mailto:support@datacater.io">
                  support
                </a>{" "}
                if the issues persist.
              </div>
            )}
            <form>
              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  name="username"
                  className="form-control"
                  value={this.state.formValues.username}
                  onChange={this.handleChange}
                />
              </div>

              <div className="form-group mt-2">
                <label>Password</label>
                <input
                  type="password"
                  name="password"
                  className="form-control form-control-appended"
                  placeholder="Enter your password"
                  value={this.state.formValues.password}
                  onChange={this.handleChange}
                />
              </div>

              <button
                className="mt-4 btn btn-lg w-100 btn-primary my-3 text-white"
                onClick={this.handleLogin}
              >
                Sign in
              </button>

              <div className="d-lg-none mt-4 text-center">
                <a href="https://datacater.io/privacy_policy/">
                  Privacy policy
                </a>
                <a className="ms-4" href="mailto:support@datacater.io">
                  Support
                </a>
                <a className="ms-4" href="https://datacater.io/imprint/">
                  Imprint
                </a>
              </div>
            </form>
          </div>
          <div className="col-12 col-md-7 col-lg-6 col-xl-8 d-none d-lg-block px-lg-0">
            <div
              className="sign-in-cover h-100 min-vh-100 d-flex justify-content-center align-items-center"
              style={{ backgroundImage: "url(/images/bg-overlay.jpg)" }}
            ></div>
          </div>
        </div>
        <div
          className="text-center w-100 pb-3 d-none d-lg-block"
          style={{ position: "fixed", bottom: "0" }}
        >
          <a
            className="text-decoration-none text-white me-4"
            href="https://datacater.io/privacy_policy/"
          >
            Privacy policy
          </a>
          <a
            className="text-decoration-none text-white me-4"
            href="mailto:support@datacater.io"
          >
            Support
          </a>
          <a
            className="text-decoration-none text-white"
            href="https://datacater.io/imprint/"
          >
            Imprint
          </a>
        </div>
      </React.Fragment>
    );
  }
}

const mapStateToProps = function (state) {
  return {
    userSessions: state.userSessions,
  };
};

const mapDispatchToProps = {
  signIn: signIn,
};

export default connect(mapStateToProps, mapDispatchToProps)(Login);

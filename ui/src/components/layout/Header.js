import React, { Component } from "react";
import { Copy } from "react-feather";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";

class Header extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showApiCall: false,
      showToken: false,
    };
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
    this.toggleShowToken = this.toggleShowToken.bind(this);
  }

  toggleShowToken(event) {
    event.preventDefault();

    this.setState({
      showToken: !this.state.showToken,
    });
  }

  toggleShowApiCall(event) {
    event.preventDefault();

    const newShowApiCall = !this.state.showApiCall;

    if (this.props.updateCallback !== undefined) {
      this.props.updateCallback(newShowApiCall);
    }

    this.setState({
      showApiCall: newShowApiCall,
    });
  }

  render() {
    const {
      apiDocs,
      apiPath,
      buttons,
      httpMethod,
      requestBody,
      subTitle,
      title,
    } = this.props;

    const token = this.state.showToken
      ? localStorage.getItem("userToken")
      : "YOUR_TOKEN";
    const tokenClassName = this.state.showToken ? "" : "text-purple";

    let curlCommand = `curl ${getApiPathPrefix(
      true
    )}${apiPath} -H'Authorization: Bearer ${token}'`;

    if (httpMethod !== undefined) {
      curlCommand = curlCommand + ` -X${httpMethod}`;
    }

    if (requestBody !== undefined) {
      curlCommand =
        curlCommand +
        ` -H'Content-Type:application/json' -d'${JSON.stringify(requestBody)}'`;
    }

    return (
      <div className="col-12 mt-3">
        <div className="card welcome-card py-2 bg-gradient-purple">
          <div className="card-body text-center p-0">
            <div className="row justify-content-center align-items-center">
              <div className="col-12 col-lg-6 mb-2 mb-lg-0 text-start">
                <h4 className="fw-semibold mb-0">{title}</h4>
                {subTitle !== undefined && (
                  <p className="text-white mb-0">{subTitle}</p>
                )}
              </div>
              <div className="col-12 col-lg-6 d-flex align-items-center justify-content-lg-end">
                <div>
                  <a
                    href="#"
                    className="btn btn-light btn-pill"
                    onClick={this.toggleShowApiCall}
                  >
                    {this.state.showApiCall ? "Hide" : "Show"} API call
                  </a>
                  {buttons}
                </div>
              </div>
            </div>
            {this.state.showApiCall && (
              <div className="bg-black mx-n3 p-3 mt-2 mb-n3 text-start position-relative">
                <p
                  className="position-absolute"
                  style={{ right: "5px", top: "5px" }}
                >
                  <a
                    href="#"
                    target="_blank"
                    rel="noreferrer"
                    className="btn btn-sm btn-light me-2"
                    onClick={(e) => {
                      e.preventDefault();
                      navigator.clipboard.writeText(curlCommand);
                    }}
                  >
                    <Copy className="feather-icon" />
                  </a>
                  <a
                    href="#"
                    className="btn btn-sm btn-light me-2"
                    onClick={this.toggleShowToken}
                  >
                    {!this.state.showToken && "Show token"}
                    {this.state.showToken && "Hide token"}
                  </a>
                  <a
                    href={apiDocs}
                    target="_blank"
                    rel="noreferrer"
                    className="btn btn-sm btn-light"
                  >
                    Docs
                  </a>
                </p>
                <pre className="mb-0">
                  <code className="text-white">
                    <span className="text-blue-light">$</span> curl{" "}
                    {getApiPathPrefix(true)}
                    {apiPath} \<br />
                    {httpMethod !== undefined && (
                      <>
                        <span className="me-2"></span> -X{httpMethod} \<br />
                      </>
                    )}
                    {requestBody !== undefined && (
                      <>
                        <span className="me-2"></span>{" "}
                        -H&apos;Content-Type:application/json&apos; \<br />
                        <span className="me-2"></span> -d&apos;
                        {JSON.stringify(requestBody)}&apos; \<br />
                      </>
                    )}
                    <span className="me-2"></span> -H&apos;Authorization:Bearer{" "}
                    <span className={tokenClassName}>{token}</span>&apos;
                    <br />
                  </code>
                </pre>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }
}

export default Header;

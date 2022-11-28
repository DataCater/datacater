import React, { Component } from "react";
import { Copy } from "react-feather";
import { getApiPathPrefix } from "../../helpers/getApiPathPrefix";

class Header extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showToken: false,
    };
    this.toggleShowToken = this.toggleShowToken.bind(this);
  }

  toggleShowToken(event) {
    event.preventDefault();

    this.setState({
      showToken: !this.state.showToken,
    });
  }

  render() {
    const { apiDocs, apiPath, httpMethod, requestBody } = this.props;

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
      <div className="bg-black p-3 text-start position-relative">
        <p className="position-absolute" style={{ right: "16px", top: "5px" }}>
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
    );
  }
}

export default Header;

import React, { Component } from "react";
import ApiCall from "./ApiCall";

class Header extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showApiCall: false,
    };
    this.toggleShowApiCall = this.toggleShowApiCall.bind(this);
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
                    className="btn btn-light"
                    onClick={this.toggleShowApiCall}
                  >
                    {this.state.showApiCall ? "Hide" : "Show"} API call
                  </a>
                  {buttons}
                </div>
              </div>
            </div>
            {this.state.showApiCall && (
              <div className="mx-n3 mt-2 mb-n3">
                <ApiCall
                  apiDocs={apiDocs}
                  apiPath={apiPath}
                  buttons={buttons}
                  httpMethod={httpMethod}
                  requestBody={requestBody}
                />
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }
}

export default Header;

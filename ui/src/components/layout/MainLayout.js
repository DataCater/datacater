import React, { Component } from "react";
import Navigation from "./Navigation";

class MainLayout extends Component {
  render() {
    // render sign in form if user is not authenticated
    if (
      window.location.pathname.includes("sign_in") ||
      window.location.pathname === "/404"
    ) {
      return <React.Fragment>{this.props.children}</React.Fragment>;
    }

    return (
      <React.Fragment>
        <Navigation />
        {this.props.children}
      </React.Fragment>
    );
  }
}

export default MainLayout;

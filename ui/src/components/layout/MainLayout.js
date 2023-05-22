import React, { Component } from "react";
import Navigation from "./Navigation";

class MainLayout extends Component {
  render() {
    const pathname = window.location.pathname;

    // render sign in form if user is not authenticated
    if (pathname.includes("sign_in") || pathname === "/404") {
      return <React.Fragment>{this.props.children}</React.Fragment>;
    }

    return (
      <React.Fragment>
        <Navigation pathname={pathname} />
        {this.props.children}
      </React.Fragment>
    );
  }
}

export default MainLayout;

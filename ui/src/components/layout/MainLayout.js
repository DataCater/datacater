import React, { Component } from "react";
import Navigation from "./Navigation";
import Footer from "./Footer";

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
        <Footer />
      </React.Fragment>
    );
  }
}

export default MainLayout;

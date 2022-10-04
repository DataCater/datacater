import React, { Component } from "react";

class FatalError extends Component {
  componentDidMount() {
    // do a small trick to add CSS classes to dom elements outside of
    // the application's root element
    const bodyDomElement = document.querySelector("body");
    bodyDomElement.className = "d-flex h-100 text-center text-bg-spacy";
    const rootDomElement = document.querySelector("#root");
    rootDomElement.classList.add("container");
    const htmlDomElement = document.querySelector("html");
    htmlDomElement.classList.add("h-100");
  }

  componentWillUnmount() {
    // get rid of the added CSS classes
    const bodyDomElement = document.querySelector("body");
    bodyDomElement.className = "";
    const rootDomElement = document.querySelector("#root");
    rootDomElement.classList.remove("container");
    const htmlDomElement = document.querySelector("html");
    htmlDomElement.classList.remove("h-100");
  }

  render() {
    return (
      <div className="cover-container d-flex w-100 h-100 p-3 mx-auto flex-column">
        <main className="px-3 my-auto">
          <h1>500</h1>
          <p className="lead">A fatal error happened.</p>
          <p className="lead">
            <a
              href="/"
              className="btn btn-lg btn-secondary fw-bold border-white bg-white"
            >
              Return to the startpage
            </a>
          </p>
        </main>
      </div>
    );
  }
}

export default FatalError;

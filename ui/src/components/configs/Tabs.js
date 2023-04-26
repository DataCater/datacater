import React, { Component } from "react";

class Tabs extends Component {
  render() {
    const { currentTab, updateTabFunc } = this.props;

    return (
      <ul className="nav flex-column nav-pills mt-4">
        {currentTab === "config" && (
          <>
            <li className="nav-item">
              <a
                className="nav-link active"
                href="#"
                data-tab="config"
                onClick={updateTabFunc}
              >
                Config
              </a>
            </li>
            <li className="nav-item mt-2">
              <a
                className="nav-link"
                href="#"
                data-tab="spec"
                onClick={updateTabFunc}
              >
                Spec
              </a>
            </li>
          </>
        )}
        {currentTab === "spec" && (
          <>
            <li className="nav-item">
              <a
                className="nav-link"
                href="#"
                data-tab="config"
                onClick={updateTabFunc}
              >
                Config
              </a>
            </li>
            <li className="nav-item mt-2">
              <a
                className="nav-link active"
                href="#"
                data-tab="spec"
                onClick={updateTabFunc}
              >
                Spec
              </a>
            </li>
          </>
        )}
      </ul>
    );
  }
}

export default Tabs;

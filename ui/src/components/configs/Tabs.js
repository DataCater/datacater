import React, { Component } from "react";

class Tabs extends Component {
  render() {
    const { currentTab, updateTabFunc } = this.props;

    return (
      <ul className="nav flex-column nav-pills mt-4">
        {currentTab === "config" && (
          <>
            <li className="nav-item">
              <button
                className="nav-link w-100 text-start active"
                data-tab="config"
                onClick={updateTabFunc}
              >
                Config
              </button>
            </li>
            <li className="nav-item mt-2">
              <button
                className="nav-link w-100 text-start"
                data-tab="spec"
                onClick={updateTabFunc}
              >
                Spec
              </button>
            </li>
          </>
        )}
        {currentTab === "spec" && (
          <>
            <li className="nav-item">
              <button
                className="nav-link w-100 text-start"
                data-tab="config"
                onClick={updateTabFunc}
              >
                Config
              </button>
            </li>
            <li className="nav-item mt-2">
              <button
                className="nav-link w-100 text-start active"
                data-tab="spec"
                onClick={updateTabFunc}
              >
                Spec
              </button>
            </li>
          </>
        )}
      </ul>
    );
  }
}

export default Tabs;

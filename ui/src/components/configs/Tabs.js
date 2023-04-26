import React, { Component } from "react";

class Tabs extends Component {
  render() {
    const { currentTab, updateTabFunc } = this.props;

    return (
      <ul class="nav flex-column nav-pills mt-4">
        {currentTab === "config" && (
          <>
            <li class="nav-item">
              <a
                class="nav-link active"
                href="#"
                data-tab="config"
                onClick={updateTabFunc}
              >
                Config
              </a>
            </li>
            <li class="nav-item mt-2">
              <a
                class="nav-link"
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
            <li class="nav-item">
              <a
                class="nav-link"
                href="#"
                data-tab="config"
                onClick={updateTabFunc}
              >
                Config
              </a>
            </li>
            <li class="nav-item mt-2">
              <a
                class="nav-link active"
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

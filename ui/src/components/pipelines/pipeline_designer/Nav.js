import React, { Component } from "react";
import {
  Code,
  Filter,
  Home,
  PlayCircle,
  Search,
  Settings,
} from "react-feather";

class Nav extends Component {
  constructor(props) {
    super(props);

    this.moveToPage = this.moveToPage.bind(this);
  }

  moveToPage(targetPage) {
    if (document.querySelector(".loading-pipeline-designer-wrapper") != null) {
      document
        .querySelector(".loading-pipeline-designer-wrapper")
        .classList.add("d-block");
    }

    setTimeout(() => {
      this.props.moveToPageFunc(targetPage, 0);
    }, 100);
  }

  render() {
    let exploreClassNames = "nav-link text-black me-2";
    let filterClassNames = "nav-link text-black me-2";
    let transformClassNames = "nav-link text-black me-2";

    if (this.props.currentPage === "explore") {
      exploreClassNames += " active text-white";
    }

    if (this.props.currentPage === "filter") {
      filterClassNames += " active text-white";
    }

    if (this.props.currentPage === "transform") {
      transformClassNames += " active text-white";
    }

    return (
      <ul className="nav nav-pills">
        <li className="nav-item">
          <a
            className={exploreClassNames}
            href={window.location.href}
            onClick={(e) => {
              e.preventDefault();
              this.moveToPage("explore");
            }}
          >
            <Search className="feather-icon me-1" />
            Inspect source
          </a>
        </li>
        <li className="nav-item">
          <a
            className={transformClassNames}
            href={window.location.href}
            onClick={(e) => {
              e.preventDefault();
              this.moveToPage("transform");
            }}
          >
            <Code className="feather-icon me-1" />
            Transform
          </a>
        </li>
      </ul>
    );
  }
}

export default Nav;

import React, { Component } from "react";

class ContextBarSizer extends Component {
  render() {
    const { changeContextBarSizeFunc, contextBarSize } = this.props;

    const sClassNames =
      contextBarSize === "S"
        ? "btn btn-sm btn-dark me-2"
        : "btn btn-sm btn-outline-dark me-2";
    const mClassNames =
      contextBarSize === "M"
        ? "btn btn-sm btn-dark me-2"
        : "btn btn-sm btn-outline-dark me-2";
    const lClassNames =
      contextBarSize === "L"
        ? "btn btn-sm btn-dark me-2"
        : "btn btn-sm btn-outline-dark me-2";

    return (
      <React.Fragment>
        <a
          href="#"
          className={sClassNames}
          data-size="S"
          onClick={changeContextBarSizeFunc}
        >
          S
        </a>
        <a
          href="#"
          className={mClassNames}
          data-size="M"
          onClick={changeContextBarSizeFunc}
        >
          M
        </a>
        <a
          href="#"
          className={lClassNames}
          data-size="L"
          onClick={changeContextBarSizeFunc}
        >
          L
        </a>
      </React.Fragment>
    );
  }
}

export default ContextBarSizer;

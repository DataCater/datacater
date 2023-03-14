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
        <button
          className={sClassNames}
          data-size="S"
          onClick={changeContextBarSizeFunc}
        >
          S
        </button>
        <button
          className={mClassNames}
          data-size="M"
          onClick={changeContextBarSizeFunc}
        >
          M
        </button>
        <button
          className={lClassNames}
          data-size="L"
          onClick={changeContextBarSizeFunc}
        >
          L
        </button>
      </React.Fragment>
    );
  }
}

export default ContextBarSizer;

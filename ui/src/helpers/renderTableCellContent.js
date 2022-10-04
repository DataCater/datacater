import React from "react";

export function renderTableCellContent(rawValue) {
  let cellContent = <React.Fragment></React.Fragment>;

  const renderString = function (value) {
    if (typeof value != "string") {
      return value;
    }

    const tokens = value.split(" ");

    if (tokens.length === 0) {
      return value;
    } else {
      return (
        <React.Fragment>
          {tokens.map((entry, index) => (
            <React.Fragment key={index}>
              {index > 0 && <span className="text-black-50">·</span>}
              {entry}
            </React.Fragment>
          ))}
        </React.Fragment>
      );
    }
  };

  if (rawValue == null) {
    cellContent = <span className="text-black-50">null</span>;
  } else if (typeof rawValue === "boolean") {
    if (rawValue === true) {
      cellContent = "true";
    } else if (rawValue === false) {
      cellContent = "false";
    } else {
      cellContent = "";
    }
  } else if (Array.isArray(rawValue)) {
    cellContent = (
      <React.Fragment>
        <span className="text-black-50">(</span>
        {rawValue.map((entry, index) => (
          <React.Fragment key={index}>
            {index > 0 && <span className="text-black-50">,</span>}
            {renderTableCellContent(entry)}
          </React.Fragment>
        ))}
        <span className="text-black-50">)</span>
      </React.Fragment>
    );
  } else if (
    typeof rawValue === "object" &&
    !Array.isArray(rawValue) &&
    rawValue != null
  ) {
    cellContent = JSON.stringify(rawValue);
  } else {
    cellContent = renderString(rawValue);
  }

  return cellContent;
}

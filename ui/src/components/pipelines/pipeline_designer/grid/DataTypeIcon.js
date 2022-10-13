import React, { Component } from "react";
import { Button, Modal } from "react-bootstrap";
import { Check, Code, Hash, HelpCircle, List, Type } from "react-feather";

class DataTypeIcon extends Component {
  render() {
    let dataTypeIcon = <HelpCircle className="feather-icon" />;

    switch (this.props.dataType) {
      case "array":
        dataTypeIcon = (
          <span title="Array">
            <List className="feather-icon" />
          </span>
        );
        break;
      case "object":
        dataTypeIcon = (
          <span title="Object">
            <Code className="feather-icon" />
          </span>
        );
        break;
      case "boolean":
        dataTypeIcon = (
          <span title="Boolean">
            <Check className="feather-icon" />
          </span>
        );
        break;
      case "number":
        dataTypeIcon = (
          <span title="Number">
            <Hash className="feather-icon" />
          </span>
        );
        break;
      case "string":
        dataTypeIcon = (
          <span title="String">
            <Type className="feather-icon" />
          </span>
        );
        break;
      default:
        break;
    }

    return dataTypeIcon;
  }
}

export default DataTypeIcon;

import React, { Component } from "react";
import { Button } from "react-bootstrap";
import { Package } from "react-feather";

class NewAttributeForm extends Component {
  render() {
    const { attribute, attributes, canEditPipeline, pipelineStep, transforms } =
      this.props;

    const typeCastTransformer = transforms.find(
      (_) => _.key === "cast-data-type"
    );

    const isValidAttributeName = isValidAttributeNameCheck(
      attribute.actionValue
    );

    const isDuplicateName = attributes
      .filter(
        (attr) => parseInt(attr.id) !== parseInt(attribute.transformAttributeId)
      )
      .map((attr) => attr.name)
      .includes(attribute.actionValue);

    return (
      <React.Fragment>
        <div className="datacater-context-bar-fixed-double-element border-bottom">
          <div className="datacater-context-bar-fixed-element w-100">
            <h3 className="mb-0 text-nowrap">
              <Package className="feather-icon me-2 mt-n1" />
              New attribute
            </h3>
          </div>
          <div className="datacater-context-bar-fixed-element w-100 datacater-context-bar-search-field">
            <Button disabled={true} className="delete-btn w-100" variant="link">
              Reset transformation
            </Button>
          </div>
        </div>
        <div className="form-group mb-0 py-4 datacater-context-bar-function-config">
          <div className="form-group mb-3">
            <label>Attribute name:</label>
            <input
              className={textFieldClassNames}
              data-attribute-id={attribute.transformAttributeId}
              data-sort-position={pipelineStep.sortPosition}
              disabled={!canEditPipeline}
              name="actionValue"
              onChange={this.props.handleChangeFunc}
              placeholder="Attribute name"
              type="text"
              value={attribute.actionValue}
            />
          </div>
          <div className="form-group mb-3">
            <label>Data type:</label>
            <select
              className="custom-select"
              data-attribute-id={attribute.transformAttributeId}
              data-sort-position={pipelineStep.sortPosition}
              disabled={!canEditPipeline}
              name="filterValue"
              onChange={this.props.handleChangeFunc}
              value={attribute.filterValue}
            >
              {typeCastTransformer.actionValueOptions.map((value, index) => (
                <option key={index} value={value.value}>
                  {value.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </React.Fragment>
    );
  }
}

export default NewAttributeForm;

import React, { Component } from "react";
import { humanizeDataType } from "../../../../../helpers/humanizeDataType";
import AttributeTransformationList from "./AttributeTransformationList";
import DataTypeIcon from "../../grid/DataTypeIcon";
import Config from "./Config";

class Attribute extends Component {
  render() {
    const {
      attributes,
      currentStep,
      editColumn,
      filters,
      handleChangeFunc,
      profile,
      sortPosition,
      transformStep,
      transforms,
    } = this.props;

    const attribute = attributes.find(
      (attr) => attr === editColumn.attributeName
    );

    const attributeProfile = profile[attribute];

    const pipelineAttribute = transformStep.transformations.find(
      (transform) => transform.attributeName === attribute
    );

    let transform = undefined;
    let filter = undefined;
    if (pipelineAttribute !== undefined) {
      transform = transforms.find(
        (transform) => transform.key === pipelineAttribute.transformation
      );
      filter = filters.find(
        (filter) => filter.key === pipelineAttribute.filter
      );
    }

    return (
      <React.Fragment>
        <div className="row border-bottom py-4">
          <div className="col ps-0">
            <h3 className="mb-0 overflow-hidden text-nowrap d-flex align-items-center">
              <DataTypeIcon dataType={attributeProfile.dataType} />{" "}
              <span className="ms-1">{attribute}</span>
            </h3>
          </div>
        </div>
        {transform == null && (
          <AttributeTransformationList
            attribute={attribute}
            attributeDataType={attributeProfile.dataType}
            currentStep={currentStep}
            handleChangeFunc={handleChangeFunc}
            transformStep={transformStep}
            transformersForAttribute={transforms}
          />
        )}
        {transform != null && (
          <Config
            attribute={attribute}
            attributes={attributes}
            attributeDataType={attributeProfile.dataType}
            currentStep={currentStep}
            editColumn={editColumn}
            filter={filter}
            filters={filters}
            handleChangeFunc={handleChangeFunc}
            pipelineAttribute={pipelineAttribute}
            previewState={this.props.previewState}
            sortPosition={sortPosition}
            transform={transform}
            transforms={transforms}
            transformStep={transformStep}
          />
        )}
      </React.Fragment>
    );
  }
}

export default Attribute;

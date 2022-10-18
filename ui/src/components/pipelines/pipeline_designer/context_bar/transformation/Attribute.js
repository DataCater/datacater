import React, { Component } from "react";
import { humanizeDataType } from "../../../../../helpers/humanizeDataType";
import AttributeTransformationList from "./AttributeTransformationList";
import DataTypeIcon from "../../grid/DataTypeIcon";
import Config from "./Config";

class Attribute extends Component {
  render() {
    const {
      fields,
      currentStep,
      editColumn,
      filters,
      handleChangeFunc,
      profile,
      sortPosition,
      transformStep,
      transforms,
    } = this.props;

    const field = fields.find(
      (attr) => attr === editColumn.fieldName
    );

    const fieldProfile = profile[field];

    const pipelineField = transformStep.transformations.find(
      (transform) => transform.fieldName === field
    );

    let transform = undefined;
    let filter = undefined;
    if (pipelineField !== undefined) {
      transform = transforms.find(
        (transform) => transform.key === pipelineField.transformation
      );
      filter = filters.find(
        (filter) => filter.key === pipelineField.filter
      );
    }

    return (
      <React.Fragment>
        <div className="row border-bottom py-4">
          <div className="col ps-0">
            <h3 className="mb-0 overflow-hidden text-nowrap d-flex align-items-center">
              <DataTypeIcon dataType={fieldProfile.dataType} />{" "}
              <span className="ms-1">{field}</span>
            </h3>
          </div>
        </div>
        {transform == null && (
          <AttributeTransformationList
            field={field}
            fieldDataType={fieldProfile.dataType}
            currentStep={currentStep}
            handleChangeFunc={handleChangeFunc}
            transformStep={transformStep}
            transformersForField={transforms}
          />
        )}
        {transform != null && (
          <Config
            field={field}
            fields={fields}
            fieldDataType={fieldProfile.dataType}
            currentStep={currentStep}
            editColumn={editColumn}
            filter={filter}
            filters={filters}
            handleChangeFunc={handleChangeFunc}
            pipelineField={pipelineField}
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

import Pipeline from "../pipelines/Pipeline";
import Profiler from "../data_profiler/Profiler";
import { deepCopy } from "../helpers/deepCopy";

const applyPipelineStepToAttributes = (pipelineStep, attributes) => {
  // make sure that we don't change the old attributes collection
  let temporaryAttributes = deepCopy(attributes);

  pipelineStep.attributes.forEach(function (attribute) {
    const attributeIndex = temporaryAttributes.findIndex(
      (_) => parseInt(_.id) === parseInt(attribute.transformAttributeId)
    );
    const transformAction = attribute.transformationAction;

    if (transformAction === "add-column") {
      const columnName =
        attribute.actionValue !== undefined && attribute.actionValue.length > 0
          ? attribute.actionValue
          : "attribute_" + attribute.transformAttributeId;

      temporaryAttributes.push({
        id: attribute.transformAttributeId + "",
        name: columnName,
        dataType: attribute.filterValue,
        isKey: false,
        isVirtual: true,
      });
    } else if (attributeIndex >= 0) {
      // verify that the attribute exists
      if (transformAction === "rename-column") {
        temporaryAttributes[attributeIndex].name = attribute.actionValue;
      } else if (
        transformAction === "cast-data-type" &&
        ![undefined, ""].includes(attribute.actionValue)
      ) {
        if (temporaryAttributes[attributeIndex].dataType.includes("array_")) {
          temporaryAttributes[attributeIndex].dataType =
            "array_" + attribute.actionValue;
        } else {
          temporaryAttributes[attributeIndex].dataType = attribute.actionValue;
        }
      } else if (transformAction === "drop-column") {
        temporaryAttributes.splice(attributeIndex, 1);
      } else if (transformAction === "tokenize") {
        temporaryAttributes[attributeIndex].dataType = "array_string";
      }
    }
  });

  return temporaryAttributes;
};

export function applyTransformationToCachedSampleRecords(
  executeTransformationFunc,
  pipelineDesigner,
  attributes,
  pipeline,
  currentStep,
  attributeId
) {
  const loadedSampleRecords = (sampleRecords) => ({
    type: "LOADED_SAMPLE_RECORDS",
    sampleRecords: sampleRecords,
  });

  return function (dispatch) {
    // read cached sample records
    const cachedSampleRecords = deepCopy(
      pipelineDesigner.cachedSampleRecords
        // ignore records dropped by the previous step
        .filter((record) => record != null && record.isDropped !== true)
    );

    // read current sample records (at current step)
    const currentSampleRecords = pipelineDesigner.sampleRecords;

    // determine pipeline step attributes after applying previous step
    let dataSourceProfileAttributes = deepCopy(attributes);
    for (let i = 1; i < currentStep; i++) {
      const currentPipelineStep = pipeline.pipelineSteps.find(
        (el) => el.sortPosition === i
      );
      dataSourceProfileAttributes = applyPipelineStepToAttributes(
        currentPipelineStep,
        dataSourceProfileAttributes
      );
    }

    // determine pipeline step attribute
    const pipelineStep = pipeline.pipelineSteps.find(
      (step) => step.sortPosition === currentStep
    );
    if (pipelineStep != null) {
      const dataSourceProfileAttribute = dataSourceProfileAttributes.find(
        (attribute) => parseInt(attribute.id) === parseInt(attributeId)
      );
      const transformationAttribute = pipelineStep.attributes.find(
        (attribute) =>
          parseInt(attribute.transformAttributeId) === parseInt(attributeId)
      );
      const unifiedAttribute = Object.assign(
        {},
        transformationAttribute,
        dataSourceProfileAttribute
      );

      const simulatedPipeline = new Pipeline(currentSampleRecords);

      return simulatedPipeline
        .applySingleAttributeTransformation(
          executeTransformationFunc,
          cachedSampleRecords,
          pipelineStep,
          unifiedAttribute,
          dataSourceProfileAttributes
        )
        .then((sampleRecords) => dispatch(loadedSampleRecords(sampleRecords)));
    }
  };
}

export function resetSampleRecordsCache() {
  return function (dispatch) {
    dispatch(() => ({
      type: "RESET_SAMPLE_RECORDS_CACHE",
    }));
  };
}

export function applyPipelineToSampleRecords(
  executeTransformationFunc,
  pipelineDesigner,
  originalSampleRecords,
  originalJoinedSampleRecords,
  dataSourceProfileAttributes,
  dataSourceProfileId,
  pipeline,
  currentStep,
  useCache
) {
  const loadedSampleRecords = (sampleRecords) => ({
    type: "LOADED_SAMPLE_RECORDS",
    sampleRecords: sampleRecords,
  });

  const updateSampleRecordsCache = (cachedSampleRecords) => ({
    cachedSampleRecords: cachedSampleRecords,
    cachedStep: currentStep - 1,
    type: "UPDATE_SAMPLE_RECORDS_CACHE",
  });

  const getAttributesOfPipelineStep = (
    pipelineStep,
    attributes,
    dataSourceProfileId
  ) => {
    return (
      pipelineStep.attributes
        // Filter out attributes dropped by a preceding step, but keep attributes added in the current step
        .filter(
          (attribute) =>
            // keep attributes added in the current step
            attribute.transformationAction === "add-column" ||
            // drop attributes which do no longer exist
            attributes.find(
              (attr) =>
                parseInt(attr.id) === parseInt(attribute.transformAttributeId)
            ) != null
        )
        .map(function (attribute) {
          if (attribute.transformationAction === "add-column") {
            const columnName =
              attribute.actionValue !== undefined &&
              attribute.actionValue.length > 0
                ? attribute.actionValue
                : "attribute_" + attribute.transformAttributeId;
            return Object.assign(
              {},
              {
                id: attribute.transformAttributeId + "",
                dataSourceProfileId: dataSourceProfileId,
                name: columnName,
                dataType: attribute.filterValue,
                isKey: false,
                isVirtual: true,
              },
              attribute
            );
          } else {
            const transformedAttribute = attributes.find(
              (_) => _.id === attribute.transformAttributeId + ""
            );

            const dataType =
              transformedAttribute !== undefined &&
              ![undefined, ""].includes(transformedAttribute.dataType)
                ? transformedAttribute.dataType
                : attribute.dataType;

            return Object.assign(
              {},
              attributes.find(
                (_) => _.id === attribute.transformAttributeId + ""
              ),
              attribute,
              { dataType: dataType }
            );
          }
        })
    );
  };

  return async function (dispatch) {
    if (originalSampleRecords.length > 0) {
      let attributes = deepCopy(dataSourceProfileAttributes);

      // data source view
      if (currentStep === undefined) {
        const simulatedPipeline = new Pipeline(
          deepCopy(originalSampleRecords),
          attributes,
          deepCopy(originalJoinedSampleRecords),
          pipeline.joinConfig
        );

        dispatch(loadedSampleRecords(simulatedPipeline.getSampleRecords()));
      } else {
        // filter or pipeline step view
        // determine whether we can make use of the cache
        const useSampleRecordsCache =
          useCache !== false &&
          pipelineDesigner.cachedStep !== undefined &&
          currentStep === pipelineDesigner.cachedStep + 1;

        let sampleRecords = useSampleRecordsCache
          ? deepCopy(pipelineDesigner.cachedSampleRecords)
          : deepCopy(originalSampleRecords);

        if (useSampleRecordsCache) {
          // only recalculate current step
          // gather attribute information
          for (let i = 0; i < currentStep; i++) {
            const pipelineStep = pipeline.pipelineSteps.find(
              (el) => el.sortPosition === i + 1
            );
            const stepAttributes = getAttributesOfPipelineStep(
              pipelineStep,
              attributes,
              dataSourceProfileId
            );

            if (pipelineStep.sortPosition === currentStep) {
              // apply transformations
              const simulatedPipeline = new Pipeline(sampleRecords);

              sampleRecords = await simulatedPipeline.applyPipelineStep(
                executeTransformationFunc,
                pipelineStep,
                stepAttributes
              );
            }

            // update attribute names and types
            attributes = applyPipelineStepToAttributes(
              pipelineStep,
              attributes
            );
          }
        } else {
          // recalculate all steps
          const simulatedPipeline = new Pipeline(
            sampleRecords,
            attributes,
            deepCopy(originalJoinedSampleRecords),
            pipeline.joinConfig
          );

          // apply assertions
          const pipelineAssertions = dataSourceProfileAttributes.map(function (
            attr
          ) {
            const attributeData = {
              attributeId: attr.id,
              name: attr.name,
              dataType: attr.dataType,
            };
            const assertionData = pipeline.pipelineAssertions.find(
              (el) => el.attributeId + "" === attr.id
            );
            return { ...attributeData, ...assertionData };
          });
          sampleRecords = simulatedPipeline.applyAssertions(pipelineAssertions);

          for (let i = 0; i < currentStep; i++) {
            const pipelineStep = pipeline.pipelineSteps.find(
              (el) => el.sortPosition === i + 1
            );
            const stepAttributes = getAttributesOfPipelineStep(
              pipelineStep,
              attributes,
              dataSourceProfileId
            );

            // cache sample records of preceding step
            if (currentStep === i + 1) {
              const cachedSampleRecords = deepCopy(sampleRecords);
              dispatch(updateSampleRecordsCache(cachedSampleRecords));
            }

            // apply transformations
            sampleRecords = await simulatedPipeline.applyPipelineStep(
              executeTransformationFunc,
              pipelineStep,
              stepAttributes
            );

            // update attribute names and types
            attributes = applyPipelineStepToAttributes(
              pipelineStep,
              attributes
            );
          }
        }

        dispatch(loadedSampleRecords(sampleRecords));
      }
    } else {
      dispatch(loadedSampleRecords([]));
    }

    return Promise.resolve();
  };
}

export function profileSampleRecords(
  sampleRecords,
  dataSourceProfileAttributes,
  pipeline,
  currentStep
) {
  const profileSampleRecords = () => ({
    type: "PROFILE_ATTRIBUTES",
  });

  const profiledSampleRecords = (attributeProfiles) => ({
    type: "PROFILED_ATTRIBUTES",
    attributeProfiles: attributeProfiles,
  });

  return function (dispatch) {
    dispatch(profileSampleRecords());

    const profiler = new Profiler();
    let attributes = deepCopy(dataSourceProfileAttributes);
    if (currentStep !== undefined) {
      for (let i = 0; i < currentStep; i++) {
        const pipelineStep = pipeline.pipelineSteps.find(
          (el) => el.sortPosition === i + 1
        );
        // update attribute names and types
        attributes = applyPipelineStepToAttributes(pipelineStep, attributes);
      }
    }

    const attributeProfiles = {};
    const attributeValues = {};
    attributes.forEach(function (attribute) {
      attributeValues[attribute.id] = [];
    });
    sampleRecords
      // filter records which are dropped by the current step
      .filter((record) => record != null && record.isDropped !== true)
      .forEach(function (record) {
        attributes.forEach(function (attribute) {
          let typedValue = record.values[attribute.id];
          if (["int", "long"].includes(attribute.dataType)) {
            typedValue = parseInt(typedValue);
            if (isNaN(typedValue)) {
              typedValue = null;
            }
          } else if (["float", "double"].includes(attribute.dataType)) {
            typedValue = parseFloat(typedValue);
            if (isNaN(typedValue)) {
              typedValue = null;
            }
          } else if (
            ["time-millis", "timestamp-millis", "date"].includes(
              attribute.dataType
            )
          ) {
            typedValue = new Date("" + typedValue);
            if (isNaN(typedValue)) {
              typedValue = null;
            }
          } else if (attribute.dataType === "null") {
            typedValue = null;
          }

          attributeValues[attribute.id].push(typedValue);
        });
      });
    attributes.forEach(function (attribute) {
      attributeProfiles[attribute.id] = profiler.profileAttribute(
        attributeValues[attribute.id],
        attribute.dataType
      );
    });
    dispatch(profiledSampleRecords(attributeProfiles));
  };
}

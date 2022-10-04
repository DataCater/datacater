const pipelineDesigner = (state, action) => {
  const initialState = {
    attributeProfiles: {},
    attributeProfilesPreviousStep: {},
    cachedSampleRecords: undefined,
    cachedStep: undefined,
    profilingAttributes: false,
    sampleRecords: [],
  };

  switch (action.type) {
    case "PROFILE_ATTRIBUTES":
      return {
        ...state,
        attributeProfiles: {},
        profilingAttributes: true,
      };
    case "PROFILED_ATTRIBUTES":
      return {
        ...state,
        attributeProfiles: action.attributeProfiles,
        profilingAttributes: false,
      };
    case "PROFILE_PREVIOUS_ATTRIBUTES":
      return {
        ...state,
        attributeProfilesPreviousStep: {},
      };
    case "PROFILED_PREVIOUS_ATTRIBUTES":
      return {
        ...state,
        attributeProfilesPreviousStep: action.attributeProfiles,
      };
    case "LOAD_SAMPLE_RECORDS":
      return {
        ...state,
      };
    case "LOADED_SAMPLE_RECORDS":
      return {
        ...state,
        sampleRecords: action.sampleRecords,
      };
    case "UPDATE_SAMPLE_RECORDS_CACHE":
      return {
        ...state,
        cachedSampleRecords: action.cachedSampleRecords,
        cachedStep: action.cachedStep,
      };
    case "RESET_SAMPLE_RECORDS_CACHE":
      return {
        ...state,
        cachedSampleRecords: undefined,
        cachedStep: undefined,
      };
    default:
      return state || initialState;
  }
};

export default pipelineDesigner;

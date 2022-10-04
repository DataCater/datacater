const pipelines = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    fetchingPipelines: false,
    inspectingPipeline: false,
    inspectionResult: undefined,
    pipelines: [],
    pipeline: undefined,
    updatingPipeline: false,
  };
  const localState = state || initialState;

  switch (action.type) {
    case "REQUEST_PIPELINES":
      return {
        ...state,
        errorMessage: undefined,
        fetchingPipelines: true,
        pipelines: [],
      };
    case "RECEIVE_PIPELINES":
      return {
        ...state,
        errorMessage: undefined,
        fetchingPipelines: false,
        pipelines: action.pipelines,
      };
    case "RECEIVE_PIPELINES_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingPipelines: false,
        pipelines: [],
      };
    case "REQUEST_PIPELINE":
      return {
        ...state,
        fetchingPipelines: true,
        updatingPipeline: false,
      };
    case "RECEIVE_PIPELINE":
      return {
        ...state,
        fetchingPipelines: false,
        pipeline: action.pipeline,
        updatingPipeline: false,
      };
    case "REQUEST_ADD_PIPELINE":
      return {
        ...state,
        errorMessage: undefined,
        pipeline: undefined,
        updatingPipeline: false,
      };
    case "RECEIVE_ADD_PIPELINE":
      return {
        ...state,
        errorMessage: undefined,
        pipeline: action.pipeline,
        updatingPipeline: false,
      };
    case "RECEIVE_ADD_PIPELINE_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        pipeline: undefined,
        updatingPipeline: false,
      };
    case "REQUEST_UPDATE_PIPELINE":
      return {
        ...state,
        updatingPipeline: true,
      };
    case "RECEIVE_UPDATE_PIPELINE":
      return {
        ...state,
        pipeline: action.pipeline,
        updatingPipeline: false,
      };
    case "REQUEST_PIPELINE_INSPECT":
      return {
        ...state,
        errorMessage: undefined,
        inspectingPipeline: true,
        inspectionResult: undefined,
      };
    case "RECEIVE_PIPELINE_INSPECT":
      return {
        ...state,
        errorMessage: undefined,
        inspectingPipeline: false,
        inspectionResult: action.inspectionResult,
      };
    case "RECEIVE_PIPELINE_INSPECT_FAILED":
      return {
        ...state,
        errorMessage: state.errorMessage,
        inspectingPipeline: false,
        inspectionResult: undefined,
      };
    default:
      return state || initialState;
  }
};

export default pipelines;

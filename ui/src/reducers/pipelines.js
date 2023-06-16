const pipelines = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    creatingPipeline: false,
    fetchingPipelines: false,
    inspectingPipeline: false,
    inspectingPipelineFailed: false,
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
        inspectingPipelineFailed: undefined,
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
        creatingPipeline: true,
        pipeline: undefined,
        updatingPipeline: false,
      };
    case "RECEIVE_ADD_PIPELINE":
      return {
        ...state,
        errorMessage: undefined,
        creatingPipeline: false,
        pipeline: action.pipeline,
        updatingPipeline: false,
      };
    case "RECEIVE_ADD_PIPELINE_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        creatingPipeline: false,
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
        inspectingPipelineFailed: undefined,
        inspectionResult: undefined,
      };
    case "RECEIVE_PIPELINE_INSPECT":
      return {
        ...state,
        errorMessage: undefined,
        inspectingPipeline: false,
        inspectingPipelineFailed: false,
        inspectionResult: action.inspectionResult,
      };
    case "RECEIVE_PIPELINE_INSPECT_FAILED":
      return {
        ...state,
        errorMessage: state.errorMessage,
        inspectingPipeline: false,
        inspectingPipelineFailed: true,
        inspectionResult: undefined,
      };
    default:
      return state || initialState;
  }
};

export default pipelines;

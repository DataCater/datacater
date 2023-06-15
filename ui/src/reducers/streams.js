const streams = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    creatingStream: false,
    fetchingStreams: false,
    inspectingStream: false,
    inspectionResult: undefined,
    stream: undefined,
    streams: [],
  };

  switch (action.type) {
    case "REQUEST_STREAMS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingStreams: true,
      };
    case "RECEIVE_STREAMS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingStreams: false,
        streams: action.streams,
      };
    case "RECEIVE_STREAMS_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingStreams: false,
        streams: [],
      };
    case "REQUEST_STREAM":
      return {
        ...state,
        errorMessage: undefined,
        fetchingStreams: true,
        stream: undefined,
      };
    case "RECEIVE_STREAM":
      return {
        ...state,
        errorMessage: undefined,
        fetchingStreams: false,
        stream: action.stream,
      };
    case "RECEIVE_STREAM_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingStreams: false,
        stream: undefined,
      };
    case "REQUEST_ADD_STREAM":
      return {
        ...state,
        creatingStream: true,
        errorMessage: undefined,
        stream: undefined,
      };
    case "RECEIVE_ADD_STREAM":
      return {
        ...state,
        creatingStream: false,
        errorMessage: undefined,
        stream: action.stream,
      };
    case "RECEIVE_ADD_STREAM_FAILED":
      return {
        ...state,
        creatingStream: false,
        errorMessage: action.errorMessage,
        stream: undefined,
      };
      // TODO for edit probably
    case "REQUEST_UPDATE_STREAM":
      return {
        ...state,
        errorMessage: undefined,
        stream: undefined,
      };
    case "RECEIVE_UPDATE_STREAM":
      return {
        ...state,
        errorMessage: undefined,
        stream: action.stream,
      };
    case "RECEIVE_UPDATE_STREAM_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        stream: undefined,
      };
    case "REQUEST_STREAM_INSPECT":
      return {
        ...state,
        errorMessage: undefined,
        inspectingStream: true,
        inspectionResult: undefined,
      };
    case "RECEIVE_STREAM_INSPECT":
      return {
        ...state,
        errorMessage: undefined,
        inspectingStream: false,
        inspectionResult: action.inspectionResult,
      };
    case "RECEIVE_STREAM_INSPECT_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        inspectingStream: false,
        inspectionResult: undefined,
      };
    default:
      return state || initialState;
  }
};

export default streams;

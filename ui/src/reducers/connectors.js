const connectors = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    fetchingConnectors: false,
    connector: undefined,
    connectors: [],
  };

  switch (action.type) {
    case "REQUEST_CONNECTORS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingConnectors: true,
      };
    case "RECEIVE_CONNECTORS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingConnectors: false,
        connectors: action.connectors,
      };
    case "RECEIVE_CONNECTORS_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingConnectors: false,
        connectors: [],
      };
    case "REQUEST_CONNECTOR":
      return {
        ...state,
        errorMessage: undefined,
        fetchingConnectors: true,
        connector: undefined,
      };
    case "RECEIVE_CONNECTOR":
      return {
        ...state,
        errorMessage: undefined,
        fetchingConnectors: false,
        connector: action.connector,
      };
    case "RECEIVE_CONNECTOR_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingConnectors: false,
        connector: undefined,
      };
    default:
      return state || initialState;
  }
};

export default connectors;

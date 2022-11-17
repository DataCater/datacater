const deployments = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    fetchingDeployments: false,
    deployment: undefined,
    deployments: [],
    fetchingLogs: false,
    logMessages: []
  };

  switch (action.type) {
    case "REQUEST_DEPLOYMENTS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingDeployments: true,
      };
    case "RECEIVE_DEPLOYMENTS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingDeployments: false,
        deployments: action.deployments,
      };
    case "RECEIVE_DEPLOYMENTS_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingDeployments: false,
        deployments: [],
      };
    case "REQUEST_DEPLOYMENT":
      return {
        ...state,
        errorMessage: undefined,
        fetchingDeployments: true,
        deployment: undefined,
      };
    case "RECEIVE_DEPLOYMENT":
      return {
        ...state,
        errorMessage: undefined,
        fetchingDeployments: false,
        deployment: action.deployment,
      };
    case "RECEIVE_DEPLOYMENT_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingDeployments: false,
        deployment: undefined,
      };
    case "REQUEST_ADD_DEPLOYMENT":
      return {
        ...state,
        errorMessage: undefined,
        deployment: undefined,
      };
    case "RECEIVE_ADD_DEPLOYMENT":
      return {
        ...state,
        errorMessage: undefined,
        deployment: action.deployment,
      };
    case "RECEIVE_ADD_DEPLOYMENT_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        deployment: undefined,
      };
    case "REQUEST_UPDATE_DEPLOYMENT":
      return {
        ...state,
        errorMessage: undefined,
        deployment: undefined,
      };
    case "RECEIVE_UPDATE_DEPLOYMENT":
      return {
        ...state,
        errorMessage: undefined,
        deployment: action.deployment,
      };
    case "RECEIVE_UPDATE_DEPLOYMENT_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        deployment: undefined,
      };
    case "REQUEST_LOGS_DEPLOYMENT":
      return {
        ...state,
        errorMessage: undefined,
        fetchingLogMessages: true,
        logMessages: [],
      };
    case "RECEIVE_LOGS_DEPLOYMENT":
      const logMessages = action.logMessages
        // TODO: Remove once API returns valid JSON not ND-JSON
        .split("\n")
        .slice(-101, -1)
        .map(logLine => {
          return JSON.parse(logLine);
        });
      return {
        ...state,
        errorMessage: undefined,
        fetchingLogMessages: false,
        logMessages: logMessages,
      };
    case "RECEIVE_LOGS_DEPLOYMENT_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingLogMessages: false,
        logMessages: [],
      };
    default:
      return state || initialState;
  }
};

export default deployments;

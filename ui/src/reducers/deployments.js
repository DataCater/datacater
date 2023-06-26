const deployments = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    creatingDeployment: false,
    updatingDeployment: false,
    fetchingDeployments: false,
    deployment: undefined,
    deployments: [],
    fetchingLogs: false,
    health: undefined,
    logMessages: [],
    metrics: undefined,
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
        creatingDeployment: true,
        errorMessage: undefined,
        deployment: undefined,
      };
    case "RECEIVE_ADD_DEPLOYMENT":
      return {
        ...state,
        creatingDeployment: false,
        errorMessage: undefined,
        deployment: action.deployment,
      };
    case "RECEIVE_ADD_DEPLOYMENT_FAILED":
      return {
        ...state,
        creatingDeployment: false,
        errorMessage: action.errorMessage,
        deployment: undefined,
      };
    case "REQUEST_UPDATE_DEPLOYMENT":
      return {
        ...state,
        updatingDeployment: true,
        errorMessage: undefined,
        deployment: undefined,
      };
    case "RECEIVE_UPDATE_DEPLOYMENT":
      return {
        ...state,
        updatingDeployment: false,
        errorMessage: undefined,
        deployment: action.deployment,
      };
    case "RECEIVE_UPDATE_DEPLOYMENT_FAILED":
      return {
        ...state,
        updatingDeployment: false,
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
      const logMessages = action.logMessages.slice(-101, -1).map((logLine) => {
        // By default, the API returns log messages in the JSON format
        //
        // In rare cases, Kubernetes mixes the JSON format with a pure text format.
        // In such a case, we need to manually construct JSON objects, so we do not break the UI
        try {
          return JSON.parse(logLine);
        } catch (error) {
          return {
            message: logLine,
          };
        }
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
    case "RESET_HEALTH_DEPLOYMENT":
      return {
        ...state,
        health: undefined,
      };
    case "REQUEST_HEALTH_DEPLOYMENT":
      return {
        ...state,
      };
    case "RECEIVE_HEALTH_DEPLOYMENT":
      return {
        ...state,
        health: action.health,
      };
    case "RECEIVE_HEALTH_DEPLOYMENT_FAILED":
      return {
        ...state,
        health: undefined,
      };
    case "RESET_METRICS_DEPLOYMENT":
      return {
        ...state,
        metrics: undefined,
      };
    case "REQUEST_METRICS_DEPLOYMENT":
      return {
        ...state,
      };
    case "RECEIVE_METRICS_DEPLOYMENT":
      return {
        ...state,
        metrics: action.metrics,
      };
    case "RECEIVE_METRICS_DEPLOYMENT_FAILED":
      return {
        ...state,
        metrics: undefined,
      };
    default:
      return state || initialState;
  }
};

export default deployments;

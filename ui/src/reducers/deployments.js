const deployments = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    fetchingDeployments: false,
    deployment: undefined,
    deployments: [],
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
    default:
      return state || initialState;
  }
};

export default deployments;

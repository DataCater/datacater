const configs = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    fetchingConfigs: false,
    config: undefined,
    configs: [],
  };

  switch (action.type) {
    case "REQUEST_CONFIGS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingConfigs: true,
      };
    case "RECEIVE_CONFIGS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingConfigs: false,
        configs: action.configs,
      };
    case "RECEIVE_CONFIGS_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingConfigs: false,
        configs: [],
      };
    case "REQUEST_CONFIG":
      return {
        ...state,
        errorMessage: undefined,
        fetchingConfigs: true,
        config: undefined,
      };
    case "RECEIVE_CONFIG":
      return {
        ...state,
        errorMessage: undefined,
        fetchingConfigs: false,
        config: action.config,
      };
    case "RECEIVE_CONFIG_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingConfigs: false,
        config: undefined,
      };
    case "REQUEST_ADD_CONFIG":
      return {
        ...state,
        errorMessage: undefined,
        config: undefined,
      };
    case "RECEIVE_ADD_CONFIG":
      return {
        ...state,
        errorMessage: undefined,
        config: action.config,
      };
    case "RECEIVE_ADD_CONFIG_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        config: undefined,
      };
    case "REQUEST_UPDATE_CONFIG":
      return {
        ...state,
        errorMessage: undefined,
        config: undefined,
      };
    case "RECEIVE_UPDATE_CONFIG":
      return {
        ...state,
        errorMessage: undefined,
        config: action.config,
      };
    case "RECEIVE_UPDATE_CONFIG_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        config: undefined,
      };
    default:
      return state || initialState;
  }
};

export default configs;

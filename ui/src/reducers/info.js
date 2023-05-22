const info = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    info: undefined,
  };

  switch (action.type) {
    case "REQUEST_INFO":
      return {
        ...state,
        errorMessage: undefined,
        info: undefined,
      };
    case "RECEIVE_INFO":
      return {
        ...state,
        errorMessage: undefined,
        info: action.info,
      };
    case "RECEIVE_INFO_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        info: undefined,
      };
    default:
      return state || initialState;
  }
};

export default info;

const userSessions = (state, action) => {
  const initialState = {
    signingIn: false,
    token: undefined,
    userIsAuthenticated: false,
    signingUp: false,
    signUpWasSuccessful: undefined,
  };

  switch (action.type) {
    case "REQUEST_SIGN_IN":
      return {
        ...state,
        signingIn: true,
      };
    case "RECEIVE_SIGN_IN":
      return {
        ...state,
        signingIn: false,
        token: action.token.access_token,
        userIsAuthenticated:
          action.token.access_token != null &&
          action.token.access_token.length > 0,
      };
    case "FAILURE_SIGN_IN":
      return initialState;
    case "REQUEST_SIGN_UP":
      return {
        ...state,
        signingUp: true,
        signUpWasSuccessful: undefined,
      };
    case "RECEIVE_SIGN_UP":
      return {
        ...state,
        signingUp: false,
        signUpWasSuccessful: true,
      };
    case "FAILURE_SIGN_UP":
      return {
        ...state,
        signingUp: false,
        signUpWasSuccessful: false,
      };
    case "SIGN_OUT":
      return initialState;
    default:
      return state || initialState;
  }
};

export default userSessions;

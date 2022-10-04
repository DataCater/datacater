import { callApi } from "../helpers/callApi";

export function signIn(username, password) {
  const requestSignIn = () => ({
    type: "REQUEST_SIGN_IN",
  });

  const receivedSignIn = (response) => ({
    type: "RECEIVE_SIGN_IN",
    token: response,
  });

  const failedSignIn = () => ({
    type: "FAILURE_SIGN_IN",
  });

  return function (dispatch) {
    dispatch(requestSignIn());

    return callApi("/authentication", {
      auth: {
        username: username,
        password: password,
      },
      cache: "no-cache",
      credentials: "same-origin",
      method: "post",
      mode: "cors",
    })
      .then((response) => {
        return response.data;
      })
      .catch((error) => {
        return dispatch(failedSignIn());
      })
      .then((json) => {
        localStorage.setItem("userToken", json.access_token);
        dispatch(receivedSignIn(json));
      });
  };
}

export function signOut() {
  localStorage.removeItem("userToken");

  return function (dispatch) {
    dispatch(() => ({ type: "SIGN_OUT" }));
  };
}

import { callApi } from "../helpers/callApi";

export function fetchInfo() {
  const requestInfo = () => ({
    type: "REQUEST_INFO",
  });

  const receivedInfo = (response) => ({
    type: "RECEIVE_INFO",
    info: response,
  });

  const receivedInfoFailed = (response) => ({
    type: "RECEIVE_INFO_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestInfo());

    return callApi("/").then(
      (response) => dispatch(receivedInfo(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(receivedInfoFailed(JSON.stringify(error.response.data)));
        }
      }
    );
  };
}

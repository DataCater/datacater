import { callApi } from "../helpers/callApi";

export function fetchTransforms() {
  const requestTransforms = () => ({
    type: "REQUEST_TRANSFORMS",
  });

  const receivedTransforms = (response) => ({
    type: "RECEIVE_TRANSFORMS",
    transforms: response,
  });

  const receivedTransformsFailed = (response) => ({
    type: "RECEIVE_TRANSFORMS_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestTransforms());

    return callApi("/transforms").then(
      (response) => dispatch(receivedTransforms(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(
            receivedTransformsFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

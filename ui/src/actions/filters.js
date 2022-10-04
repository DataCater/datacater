import { callApi } from "../helpers/callApi";

export function fetchFilters() {
  const requestFilters = () => ({
    type: "REQUEST_FILTERS",
  });

  const receivedFilters = (response) => ({
    type: "RECEIVE_FILTERS",
    filters: response,
  });

  const receivedFiltersFailed = (response) => ({
    type: "RECEIVE_FILTERS_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestFilters());

    return callApi("/filters").then(
      (response) => dispatch(receivedFilters(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(receivedFiltersFailed(JSON.stringify(error.response.data)));
        }
      }
    );
  };
}

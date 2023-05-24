import { callApi } from "../helpers/callApi";

export function fetchConnectors() {
  const requestConnectors = () => ({
    type: "REQUEST_CONNECTORS",
  });

  const receivedConnectors = (response) => ({
    type: "RECEIVE_CONNECTORS",
    connectors: response,
  });

  const receivedConnectorsFailed = (response) => ({
    type: "RECEIVE_CONNECTORS_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestConnectors());

    return callApi("/connectors").then(
      (response) => dispatch(receivedConnectors(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(
            receivedConnectorsFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function fetchConnector(id) {
  const requestConnector = () => ({
    type: "REQUEST_CONNECTOR",
  });

  const receivedConnector = (response) => ({
    type: "RECEIVE_CONNECTOR",
    connector: response,
  });

  const receivedConnectorFailed = (response) => ({
    type: "RECEIVE_CONNECTOR_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestConnector());

    return callApi(`/connectors/${id}`).then(
      (response) => dispatch(receivedConnector(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedConnectorFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function deleteConnector(id) {
  const requestDeleteConnector = () => ({
    type: "REQUEST_DELETE_CONNECTOR",
  });

  const receivedDeleteConnector = () => ({
    type: "RECEIVE_DELETE_CONNECTOR",
  });

  return function (dispatch) {
    dispatch(requestDeleteConnector());

    return callApi(`/connectors/${id}`, { method: "delete" }).then(() => {
      dispatch(receivedDeleteConnector());
    });
  };
}

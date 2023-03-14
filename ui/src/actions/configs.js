import { callApi } from "../helpers/callApi";

export function fetchConfigs() {
  const requestConfigs = () => ({
    type: "REQUEST_CONFIGS",
  });

  const receivedConfigs = (response) => ({
    type: "RECEIVE_CONFIGS",
    configs: response,
  });

  const receivedConfigsFailed = (response) => ({
    type: "RECEIVE_CONFIGS_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestConfigs());

    return callApi("/configs").then(
      (response) => dispatch(receivedConfigs(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(
            receivedConfigsFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function fetchConfig(id) {
  const requestConfig = () => ({
    type: "REQUEST_CONFIG",
  });

  const receivedConfig = (response) => ({
    type: "RECEIVE_CONFIG",
    Config: response,
  });

  const receivedConfigFailed = (response) => ({
    type: "RECEIVE_CONFIG_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestConfig());

    return callApi(`/configs/${id}`).then(
      (response) => dispatch(receivedConfig(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedConfigFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function addConfig(config) {
  const requestAddConfig = () => ({
    type: "REQUEST_ADD_CONFIG",
  });

  const receivedAddConfig = (response) => ({
    config: response,
    type: "RECEIVE_ADD_CONFIG",
  });

  const receivedAddConfigFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_ADD_CONFIG_FAILED",
  });

  return function (dispatch) {
    dispatch(requestAddConfig());

    return callApi("/configs", {
      method: "post",
      data: config,
    }).then(
      (response) => dispatch(receivedAddConfig(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedAddConfigFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function updateConfig(uuid, config) {
  const requestUpdateConfig = () => ({
    type: "REQUEST_UPDATE_CONFIG",
  });

  const receivedUpdateConfig = (response) => ({
    config: response,
    type: "RECEIVE_UPDATE_CONFIG",
  });

  const receivedUpdateConfigFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_UPDATE_CONFIG_FAILED",
  });

  return function (dispatch) {
    dispatch(requestUpdateConfig());

    return callApi(`/configs/${uuid}`, {
      method: "put",
      data: config,
    }).then(
      (response) => dispatch(receivedUpdateConfig(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedUpdateConfigFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function deleteConfig(id) {
  const requestDeleteConfig = () => ({
    type: "REQUEST_DELETE_CONFIG",
  });

  const receivedDeleteConfig = () => ({
    type: "RECEIVE_DELETE_CONFIG",
  });

  return function (dispatch) {
    dispatch(requestDeleteConfig());

    return callApi(`/configs/${id}`, { method: "delete" }).then(() => {
      dispatch(receivedDeleteConfig());
    });
  };
}

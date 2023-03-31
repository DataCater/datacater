import { callApi } from "../helpers/callApi";

export function fetchDeployments() {
  const requestDeployments = () => ({
    type: "REQUEST_DEPLOYMENTS",
  });

  const receivedDeployments = (response) => ({
    type: "RECEIVE_DEPLOYMENTS",
    deployments: response,
  });

  const receivedDeploymentsFailed = (response) => ({
    type: "RECEIVE_DEPLOYMENTS_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestDeployments());

    return callApi("/deployments").then(
      (response) => dispatch(receivedDeployments(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(
            receivedDeploymentsFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function fetchDeployment(id) {
  const requestDeployment = () => ({
    type: "REQUEST_DEPLOYMENT",
  });

  const receivedDeployment = (response) => ({
    type: "RECEIVE_DEPLOYMENT",
    deployment: response,
  });

  const receivedDeploymentFailed = (response) => ({
    type: "RECEIVE_DEPLOYMENT_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestDeployment());

    return callApi(`/deployments/${id}`).then(
      (response) => dispatch(receivedDeployment(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedDeploymentFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function addDeployment(deployment) {
  const requestAddDeployment = () => ({
    type: "REQUEST_ADD_DEPLOYMENT",
  });

  const receivedAddDeployment = (response) => ({
    deployment: response,
    type: "RECEIVE_ADD_DEPLOYMENT",
  });

  const receivedAddDeploymentFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_ADD_DEPLOYMENT_FAILED",
  });

  return function (dispatch) {
    dispatch(requestAddDeployment());

    return callApi("/deployments", {
      method: "post",
      data: deployment,
    }).then(
      (response) => dispatch(receivedAddDeployment(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedAddDeploymentFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function updateDeployment(uuid, deployment) {
  const requestUpdateDeployment = () => ({
    type: "REQUEST_UPDATE_DEPLOYMENT",
  });

  const receivedUpdateDeployment = (response) => ({
    deployment: response,
    type: "RECEIVE_UPDATE_DEPLOYMENT",
  });

  const receivedUpdateDeploymentFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_UPDATE_DEPLOYMENT_FAILED",
  });

  return function (dispatch) {
    dispatch(requestUpdateDeployment());

    return callApi(`/deployments/${uuid}`, {
      method: "put",
      data: deployment,
    }).then(
      (response) => dispatch(receivedUpdateDeployment(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedUpdateDeploymentFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function deleteDeployment(id) {
  const requestDeleteDeployment = () => ({
    type: "REQUEST_DELETE_DEPLOYMENT",
  });

  const receivedDeleteDeployment = () => ({
    type: "RECEIVE_DELETE_DEPLOYMENT",
  });

  return function (dispatch) {
    dispatch(requestDeleteDeployment());

    return callApi(`/deployments/${id}`, { method: "delete" }).then(() => {
      dispatch(receivedDeleteDeployment());
    });
  };
}

export function fetchDeploymentLogs(id, replica = 1) {
  const requestDeploymentLogs = () => ({
    type: "REQUEST_LOGS_DEPLOYMENT",
  });

  const receivedDeploymentLogs = (response) => ({
    type: "RECEIVE_LOGS_DEPLOYMENT",
    logMessages: response,
  });

  const receivedDeploymentLogsFailed = (response) => ({
    type: "RECEIVE_LOGS_DEPLOYMENT_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestDeploymentLogs());

    return callApi(`/deployments/${id}/logs?replica=${replica}`).then(
      (response) => dispatch(receivedDeploymentLogs(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedDeploymentLogsFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function fetchDeploymentHealth(id, replica = 1) {
  const requestDeploymentHealth = () => ({
    type: "REQUEST_HEALTH_DEPLOYMENT",
  });

  const receivedDeploymentHealth = (response) => ({
    type: "RECEIVE_HEALTH_DEPLOYMENT",
    health: response,
  });

  const receivedDeploymentHealthFailed = (response) => ({
    type: "RECEIVE_HEALTH_DEPLOYMENT_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestDeploymentHealth());

    return callApi(`/deployments/${id}/health?replica=${replica}`).then(
      (response) => dispatch(receivedDeploymentHealth(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(
            receivedDeploymentHealthFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function resetDeploymentHealth() {
  return function (dispatch) {
    dispatch({
      type: "RESET_HEALTH_DEPLOYMENT",
    });
  };
}

export function fetchDeploymentMetrics(id, replica = 1) {
  const requestDeploymentMetrics = () => ({
    type: "REQUEST_METRICS_DEPLOYMENT",
  });

  const receivedDeploymentMetrics = (response) => ({
    type: "RECEIVE_METRICS_DEPLOYMENT",
    metrics: response,
  });

  const receivedDeploymentMetricsFailed = (response) => ({
    type: "RECEIVE_METRICS_DEPLOYMENT_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestDeploymentMetrics());

    return callApi(`/deployments/${id}/metrics?replica=${replica}`).then(
      (response) => dispatch(receivedDeploymentMetrics(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(
            receivedDeploymentMetricsFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function resetDeploymentMetrics() {
  return function (dispatch) {
    dispatch({
      type: "RESET_METRICS_DEPLOYMENT",
    });
  };
}

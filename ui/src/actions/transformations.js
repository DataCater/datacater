export function resetTransformationError() {
  return function (dispatch) {
    dispatch({
      type: "RESET_EXECUTION_ERROR",
    });
  };
}

export function executeTransformation(code, attributeName, dataType, values) {
  const requestExecuteTransformation = () => ({
    type: "REQUEST_EXECUTION_TRANSFORMATION",
  });

  const receiveSuccessfulExecution = () => ({
    type: "RECEIVE_SUCCESSFUL_EXECUTION_TRANSFORMATION",
  });

  const receiveFailedExecution = (error) => ({
    executionError: error,
    type: "RECEIVE_FAILED_EXECUTION_TRANSFORMATION",
  });

  return function (dispatch) {
    dispatch(requestExecuteTransformation());

    const apiUri = `/api/transformations/execute_udf?dataType=${dataType}`;

    const requestBody = {
      attributeName: attributeName,
      code: code,
      values: values,
    };

    return fetch(apiUri, {
      body: JSON.stringify(requestBody),
      cache: "no-cache",
      credentials: "same-origin",
      headers: {
        "Content-type": "application/json",
        "X-Auth-token": localStorage.getItem("userToken"),
      },
      method: "POST",
      mode: "cors",
    })
      .then((response) => {
        if (response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          return response.json();
        }
      })
      .then((response) => {
        if (response.values !== undefined) {
          dispatch(receiveSuccessfulExecution());
        } else {
          dispatch(receiveFailedExecution(response.error));
        }

        return response;
      });
  };
}

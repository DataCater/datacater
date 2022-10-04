import { callApi } from "../helpers/callApi";

export function fetchStreams() {
  const requestStreams = () => ({
    type: "REQUEST_STREAMS",
  });

  const receivedStreams = (response) => ({
    type: "RECEIVE_STREAMS",
    streams: response,
  });

  const receivedStreamsFailed = (response) => ({
    type: "RECEIVE_STREAMS_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestStreams());

    return callApi("/streams").then(
      (response) => dispatch(receivedStreams(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(receivedStreamsFailed(JSON.stringify(error.response.data)));
        }
      }
    );
  };
}

export function fetchStream(id) {
  const requestStream = () => ({
    type: "REQUEST_STREAM",
  });

  const receivedStream = (response) => ({
    type: "RECEIVE_STREAM",
    stream: response,
  });

  const receivedStreamFailed = (response) => ({
    type: "RECEIVE_STREAM_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestStream());

    return callApi(`/streams/${id}`).then(
      (response) => dispatch(receivedStream(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(receivedStreamFailed(JSON.stringify(error.response.data)));
        }
      }
    );
  };
}

export function addStream(stream) {
  const requestAddStream = () => ({
    type: "REQUEST_ADD_STREAM",
  });

  const receivedAddStream = (response) => ({
    stream: response,
    type: "RECEIVE_ADD_STREAM",
  });

  const receivedAddStreamFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_ADD_STREAM_FAILED",
  });

  return function (dispatch) {
    dispatch(requestAddStream());

    return callApi("/streams", {
      method: "post",
      data: stream,
    }).then(
      (response) => dispatch(receivedAddStream(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedAddStreamFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function updateStream(uuid, stream) {
  const requestUpdateStream = () => ({
    type: "REQUEST_UPDATE_STREAM",
  });

  const receivedUpdateStream = (response) => ({
    stream: response,
    type: "RECEIVE_UPDATE_STREAM",
  });

  const receivedUpdateStreamFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_UPDATE_STREAM_FAILED",
  });

  return function (dispatch) {
    dispatch(requestUpdateStream());

    return callApi(`/streams/${uuid}`, {
      method: "put",
      data: stream,
    }).then(
      (response) => dispatch(receivedUpdateStream(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedUpdateStreamFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function deleteStream(id) {
  const requestDeleteStream = () => ({
    type: "REQUEST_DELETE_STREAM",
  });

  const receivedDeleteStream = () => ({
    type: "RECEIVE_DELETE_STREAM",
  });

  return function (dispatch) {
    dispatch(requestDeleteStream());

    return callApi(`/streams/${id}`, { method: "delete" }).then(() => {
      dispatch(receivedDeleteStream());
    });
  };
}

export function inspectStream(id, limit = 100) {
  const requestStreamInspect = () => ({
    type: "REQUEST_STREAM_INSPECT",
  });

  const receivedStreamInspect = (response) => ({
    type: "RECEIVE_STREAM_INSPECT",
    inspectionResult: response,
  });

  const receivedStreamInspectFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_STREAM_INSPECT_FAILED",
  });

  return function (dispatch) {
    dispatch(requestStreamInspect());

    return callApi(`/streams/${id}/inspect?limit=${limit}`).then(
      (response) => dispatch(receivedStreamInspect(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedStreamInspectFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

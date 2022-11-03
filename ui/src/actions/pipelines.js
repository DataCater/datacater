import { callApi } from "../helpers/callApi";

export function fetchPipelines() {
  const requestPipelines = () => ({
    type: "REQUEST_PIPELINES",
  });

  const receivedPipelines = (response) => ({
    type: "RECEIVE_PIPELINES",
    pipelines: response,
  });

  const receivedPipelinesFailed = (response) => ({
    type: "RECEIVE_PIPELINES_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestPipelines());

    return callApi("/pipelines").then(
      (response) => dispatch(receivedPipelines(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(
            receivedPipelinesFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function fetchPipeline(id) {
  const requestPipeline = () => ({
    type: "REQUEST_PIPELINE",
  });

  const receivedPipeline = (response) => ({
    type: "RECEIVE_PIPELINE",
    pipeline: response,
  });

  const receivedPipelineFailed = (response) => ({
    type: "RECEIVE_PIPELINE_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestPipeline());

    return callApi(`/pipelines/${id}`).then(
      (response) => dispatch(receivedPipeline(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(receivedPipelineFailed(JSON.stringify(error.response.data)));
        }
      }
    );
  };
}

export function addPipeline(pipeline) {
  const requestAddPipeline = () => ({
    type: "REQUEST_ADD_PIPELINE",
  });

  const receivedAddPipeline = (response) => ({
    pipeline: response,
    type: "RECEIVE_ADD_PIPELINE",
  });

  const receivedAddPipelineFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_ADD_PIPELINE_FAILED",
  });

  return function (dispatch) {
    dispatch(requestAddPipeline());

    return callApi("/pipelines", {
      method: "post",
      data: pipeline,
    }).then(
      (response) => dispatch(receivedAddPipeline(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedAddPipelineFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function updatePipeline(uuid, pipeline) {
  const requestUpdatePipeline = () => ({
    type: "REQUEST_UPDATE_PIPELINE",
  });

  const receivedUpdatePipeline = (pipeline) => ({
    pipeline: pipeline,
    type: "RECEIVE_UPDATE_PIPELINE",
  });

  const receivedUpdatePipelineFailed = () => ({
    type: "RECEIVE_UPDATE_PIPELINE_FAILED",
  });

  return function (dispatch) {
    dispatch(requestUpdatePipeline());

    return callApi(`/pipelines/${uuid}`, {
      method: "put",
      data: pipeline,
    }).then(
      (response) => dispatch(receivedUpdatePipeline(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedUpdatePipelineFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function deletePipeline(id) {
  const requestDeletePipeline = () => ({
    type: "REQUEST_DELETE_PIPELINE",
  });
  const receivedDeletePipeline = () => ({
    type: "RECEIVE_DELETE_PIPELINE",
  });

  return function (dispatch) {
    dispatch(requestDeletePipeline());

    return callApi(`/pipelines/${id}`, { method: "delete" }).then(() => {
      dispatch(receivedDeletePipeline());
    });
  };
}

export function inspectPipeline(pipeline, sampleRecords, previewStep) {
  const requestPipelineInspect = () => ({
    type: "REQUEST_PIPELINE_INSPECT",
  });

  const receivedPipelineInspect = (response) => ({
    type: "RECEIVE_PIPELINE_INSPECT",
    inspectionResult: response,
  });

  const receivedPipelineInspectFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_PIPELINE_INSPECT_FAILED",
  });

  return function (dispatch) {
    dispatch(requestPipelineInspect());

    return callApi(`/pipelines/preview/pooled`, {
      method: "post",
      data: {
        pipeline: pipeline,
        records: sampleRecords,
        previewStep: previewStep,
      },
    }).then(
      (response) => dispatch(receivedPipelineInspect(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedPipelineInspectFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

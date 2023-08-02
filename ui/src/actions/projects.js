import { callApi } from "../helpers/callApi";

export function fetchProjects() {
  const requestProjects = () => ({
    type: "REQUEST_PROJECTS",
  });

  const receivedProjects = (response) => ({
    type: "RECEIVE_PROJECTS",
    projects: response,
  });

  const receivedProjectsFailed = (response) => ({
    type: "RECEIVE_PROJECTS_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestProjects());

    return callApi("/projects").then(
      (response) => dispatch(receivedProjects(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else {
          dispatch(receivedProjectsFailed(JSON.stringify(error.response.data)));
        }
      }
    );
  };
}

export function fetchProject(id) {
  const requestProject = () => ({
    type: "REQUEST_PROJECT",
  });

  const receivedProject = (response) => ({
    type: "RECEIVE_PROJECT",
    project: response,
  });

  const receivedProjectFailed = (response) => ({
    type: "RECEIVE_PROJECT_FAILED",
    errorMessage: response,
  });

  return function (dispatch) {
    dispatch(requestProject());

    return callApi(`/projects/${id}`).then(
      (response) => dispatch(receivedProject(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(receivedProjectFailed(JSON.stringify(error.response.data)));
        }
      }
    );
  };
}

export function addProject(project) {
  const requestAddProject = () => ({
    type: "REQUEST_ADD_PROJECT",
  });

  const receivedAddProject = (response) => ({
    project: response,
    type: "RECEIVE_ADD_PROJECT",
  });

  const receivedAddProjectFailed = (response) => ({
    errorMessage: response,
    type: "RECEIVE_ADD_PROJECT_FAILED",
  });

  return function (dispatch) {
    dispatch(requestAddProject());

    return callApi("/projects", {
      method: "post",
      data: project,
    }).then(
      (response) => dispatch(receivedAddProject(response.data)),
      (error) => {
        if (error.response.status === 401) {
          localStorage.removeItem("userToken");
          window.location = "/sign_in";
        } else if (error.response.status === 404) {
          window.location = "/404";
        } else {
          dispatch(
            receivedAddProjectFailed(JSON.stringify(error.response.data))
          );
        }
      }
    );
  };
}

export function deleteProject(id) {
  const requestDeleteProject = () => ({
    type: "REQUEST_DELETE_PROJECT",
  });

  const receivedDeleteProject = () => ({
    type: "RECEIVE_DELETE_PROJECT",
  });

  return function (dispatch) {
    dispatch(requestDeleteProject());

    return callApi(`/projects/${id}`, { method: "delete" }).then(() => {
      dispatch(receivedDeleteProject());
    });
  };
}

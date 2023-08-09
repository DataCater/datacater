export function fetchProjectContext() {
  const requestProject = () => ({
    type: "GET_PROJECT",
  });

  return function (dispatch) {
    return dispatch(requestProject());
  };
}

export function updateProjectContext(project) {
  const updateProject = (response) => ({
    type: "SET_PROJECT",
    project: response,
  });

  return function (dispatch) {
    return dispatch(updateProject(project));
  };
}

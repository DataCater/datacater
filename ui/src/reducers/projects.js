const projects = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    creatingProject: false,
    updatingProject: false,
    fetchingProject: false,
    inspectingProject: false,
    inspectionResult: undefined,
    project: undefined,
    projects: [],
  };

  switch (action.type) {
    case "REQUEST_PROJECTS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingProjects: true,
      };
    case "RECEIVE_PROJECTS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingProjects: false,
        projects: action.projects,
      };
    case "RECEIVE_PROJECTS_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingProjects: false,
        projects: [],
      };
    case "REQUEST_PROJECT":
      return {
        ...state,
        errorMessage: undefined,
        fetchingProjects: true,
        project: undefined,
      };
    case "RECEIVE_PROJECT":
      return {
        ...state,
        errorMessage: undefined,
        fetchingProjects: false,
        project: action.project,
      };
    case "RECEIVE_PROJECT_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingProjects: false,
        project: undefined,
      };
    case "REQUEST_ADD_PROJECT":
      return {
        ...state,
        creatingProject: true,
        errorMessage: undefined,
        project: undefined,
      };
    case "RECEIVE_ADD_PROJECT":
      return {
        ...state,
        creatingProject: false,
        errorMessage: undefined,
        project: action.project,
      };
    case "RECEIVE_ADD_PROJECT_FAILED":
      return {
        ...state,
        creatingProject: false,
        errorMessage: action.errorMessage,
        project: undefined,
      };
    default:
      return state || initialState;
  }
};

export default projects;

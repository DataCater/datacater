import { getCurrentProjectContext } from "../helpers/getCurrentProjectContext";
import { storeCurrentProjectContext } from "../helpers/storeCurrentProjectContext";

const project = (state, action) => {
  const initialState = {
    project: "default",
  };

  switch (action.type) {
    case "GET_PROJECT":
      return {
        ...state,
        project: getCurrentProjectContext(),
      };
    case "SET_PROJECT":
      storeCurrentProjectContext(action.project);
      return {
        ...state,
        project: action.project,
      };
    default:
      return state || initialState;
  }
};

export default project;

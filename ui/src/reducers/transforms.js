const transforms = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    fetchingTransforms: false,
    transforms: [],
  };

  switch (action.type) {
    case "REQUEST_TRANSFORMS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingTransforms: true,
      };
    case "RECEIVE_TRANSFORMS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingTransforms: false,
        transforms: action.transforms.sort(
          (a, b) => (a.name > b.name) - (a.name < b.name)
        ),
      };
    case "RECEIVE_TRANSFORMS_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingTransforms: false,
        transforms: [],
      };
    default:
      return state || initialState;
  }
};

export default transforms;

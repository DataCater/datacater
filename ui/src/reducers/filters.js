const filters = (state, action) => {
  const initialState = {
    errorMessage: undefined,
    fetchingFilters: false,
    filters: [],
  };

  switch (action.type) {
    case "REQUEST_FILTERS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingFilters: true,
      };
    case "RECEIVE_FILTERS":
      return {
        ...state,
        errorMessage: undefined,
        fetchingFilters: false,
        filters: action.filters.sort(
          (a, b) => (a.name > b.name) - (a.name < b.name)
        ),
      };
    case "RECEIVE_FILTERS_FAILED":
      return {
        ...state,
        errorMessage: action.errorMessage,
        fetchingFilters: false,
        filters: [],
      };
    default:
      return state || initialState;
  }
};

export default filters;

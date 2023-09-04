import { QUERY_CONCEPTS, QUERY_CONCEPTS_OK, QUERY_CONCEPTS_ERROR, RESET_CONCEPTS_QUERY } from '../actions/Concept';

export const concept = (state = {
  conceptsQueryStarted: false,
  conceptsQueryError: false,
  results: [],
}, action) => {
  const { results, error } = action;
  switch (action.type) {
    case QUERY_CONCEPTS:
      return {
        ...state,
        conceptsQueryStarted: true,
        conceptsQueryError: false,
      };
    case QUERY_CONCEPTS_OK:
      return {
        ...state,
        conceptsQueryStarted: false,
        conceptsQueryError: false,
        results,
      };
    case QUERY_CONCEPTS_ERROR:
      return {
        ...state,
        conceptsQueryStarted: false,
        conceptsQueryError: true,
        conceptsQueryErrorDescription: error.message
      };
    case RESET_CONCEPTS_QUERY:
      return {
        ...state,
        conceptsQueryStarted: false,
        conceptsQueryError: false,
        results: []
      };
    default:
      return state;
  }
}
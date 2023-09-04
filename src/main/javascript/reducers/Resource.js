import { QUERY_RESOURCES, QUERY_RESOURCES_OK, QUERY_RESOURCES_ERROR, RESET_RESOURCES_QUERY } from '../actions/Resource';

export const resource = (state = {
  resourceQueryStarted: false,
  resourceQueryError: false,
  results: [],
}, action) => {
  const { results, error } = action;
  switch (action.type) {
    case QUERY_RESOURCES:
      return {
        ...state,
        resourceQueryStarted: true,
        resourceQueryError: false,
      };
    case QUERY_RESOURCES_OK:
      return {
        ...state,
        resourceQueryStarted: false,
        resourceQueryError: false,
        results,
      };
    case QUERY_RESOURCES_ERROR:
      return {
        ...state,
        resourceQueryStarted: false,
        resourceQueryError: true,
        resourceQueryErrorDescription: error.message
      };
    case RESET_RESOURCES_QUERY:
      return {
        ...state,
        resourceQueryStarted: false,
        resourceQueryError: false,
        results: []
      };
    default:
      return state;
  }
}
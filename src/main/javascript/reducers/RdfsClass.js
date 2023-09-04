import { QUERY_RDFS_CLASSES, QUERY_RDFS_CLASSES_OK, QUERY_RDFS_CLASSES_ERROR, RESET_RDFS_CLASSES_QUERY } from '../actions/RdfsClass';

export const rdfsClass = (state = {
  rdfsClassesQueryStarted: false,
  rdfsClassesQueryError: false,
  results: [],
}, action) => {
  const { results, error } = action;
  switch (action.type) {
    case QUERY_RDFS_CLASSES:
      return {
        ...state,
        rdfsClassesQueryStarted: true,
        rdfsClassesQueryError: false,
      };
    case QUERY_RDFS_CLASSES_OK:
      return {
        ...state,
        rdfsClassesQueryStarted: false,
        rdfsClassesQueryError: false,
        results,
      };
    case QUERY_RDFS_CLASSES_ERROR:
      return {
        ...state,
        rdfsClassesQueryStarted: false,
        rdfsClassesQueryError: true,
        rdfsClassesQueryErrorDescription: error.message
      };
    case RESET_RDFS_CLASSES_QUERY:
      return {
        ...state,
        rdfsClassesQueryStarted: false,
        rdfsClassesQueryError: false,
        results: []
      };
    default:
      return state;
  }
}
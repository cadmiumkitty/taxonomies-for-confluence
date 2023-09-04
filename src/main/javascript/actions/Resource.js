export const GET_RESOURCE = 'GET_RESOURCE';
export const GET_RESOURCE_OK = 'GET_RESOURCE_OK';
export const GET_RESOURCE_ERROR = 'GET_RESOURCE_ERROR';

export const QUERY_RESOURCES = 'QUERY_RESOURCES';
export const QUERY_RESOURCES_OK = 'QUERY_RESOURCES_OK';
export const QUERY_RESOURCES_ERROR = 'QUERY_RESOURCES_ERROR';

export const RESET_RESOURCES_QUERY = 'RESET_RESOURCES_QUERY';

export const getResource = (uri, context) => ({
  type: GET_RESOURCE,
  uri,
  context
});

export const getResourceOk = (uri, results, context) => ({
  type: GET_RESOURCE_OK,
  uri,
  results,
  context
});

export const getResourceError = (uri, error, context) => ({
  type: GET_RESOURCE_ERROR,
  uri,
  error,
  context
});

export const queryResources = (q) => ({
  type: QUERY_RESOURCES,
  q
});

export const queryResourcesOk = (results) => ({
  type: QUERY_RESOURCES_OK,
  results
});

export const queryResourcesError = (error) => ({
  type: QUERY_RESOURCES_ERROR,
  error
});

export const resetResourcesQuery = () => ({
  type: RESET_RESOURCES_QUERY
});

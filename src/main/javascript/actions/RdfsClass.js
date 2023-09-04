export const QUERY_RDFS_CLASSES = 'QUERY_RDFS_CLASSES';
export const QUERY_RDFS_CLASSES_OK = 'QUERY_RDFS_CLASSES_OK';
export const QUERY_RDFS_CLASSES_ERROR = 'QUERY_RDFS_CLASSES_ERROR';

export const RESET_RDFS_CLASSES_QUERY = 'RESET_CONCEPTS_QUERY';

export const queryRdfsClasses = (q) => ({
  type: QUERY_RDFS_CLASSES,
  q
});

export const queryRdfsClassesOk = (results) => ({
  type: QUERY_RDFS_CLASSES_OK,
  results
});

export const queryRdfsClassesError = (error) => ({
  type: QUERY_RDFS_CLASSES_ERROR,
  error
});

export const resetRdfsClassesQuery = () => ({
  type: RESET_RDFS_CLASSES_QUERY
});

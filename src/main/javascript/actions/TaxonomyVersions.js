export const GET_TAXONOMY_VERSIONS = 'GET_TAXONOMY_VERSIONS';
export const GET_TAXONOMY_VERSIONS_OK = 'GET_TAXONOMY_VERSIONS_OK';
export const GET_TAXONOMY_VERSIONS_ERROR = 'GET_TAXONOMY_VERSIONS_ERROR';

export const IMPORT_TAXONOMY_FILE = 'IMPORT_TAXONOMY_FILE';
export const IMPORT_TAXONOMY_FILE_OK = 'IMPORT_TAXONOMY_FILE_OK';
export const IMPORT_TAXONOMY_FILE_ERROR = 'IMPORT_TAXONOMY_FILE_ERROR';

export const IMPORT_TAXONOMY_CATALOG = 'IMPORT_TAXONOMY_CATALOG';
export const IMPORT_TAXONOMY_CATALOG_OK = 'IMPORT_TAXONOMY_CATALOG_OK';
export const IMPORT_TAXONOMY_CATALOG_ERROR = 'IMPORT_TAXONOMY_CATALOG_ERROR';

export const COPY_FROM_CURRENT = 'COPY_FROM_CURRENT';
export const COPY_FROM_CURRENT_OK = 'COPY_FROM_CURRENT_OK';
export const COPY_FROM_CURRENT_ERROR = 'COPY_FROM_CURRENT_ERROR';

export const CALCULATE_CONTENT_IMPACT = 'CALCULATE_CONTENT_IMPACT';
export const CALCULATE_CONTENT_IMPACT_OK = 'CALCULATE_CONTENT_IMPACT_OK';
export const CALCULATE_CONTENT_IMPACT_ERROR = 'CALCULATE_CONTENT_IMPACT_ERROR';

export const TRANSITION_TO_CURRENT = 'TRANSITION_TO_CURRENT';
export const TRANSITION_TO_CURRENT_OK = 'TRANSITION_TO_CURRENT_OK';
export const TRANSITION_TO_CURRENT_ERROR = 'TRANSITION_TO_CURRENT_ERROR';

export const CANCEL_TRANSITION_TO_CURRENT = 'CANCEL_TRANSITION_TO_CURRENT';
export const CANCEL_TRANSITION_TO_CURRENT_OK = 'CANCEL_TRANSITION_TO_CURRENT_OK';
export const CANCEL_TRANSITION_TO_CURRENT_ERROR = 'CANCEL_TRANSITION_TO_CURRENT_ERROR';

export const CLEAR = 'CLEAR';
export const CLEAR_OK = 'CLEAR_OK';
export const CLEAR_ERROR = 'CLEAR_ERROR';

export const getTaxonomyVersions = (initialRequest) => ({
  type: GET_TAXONOMY_VERSIONS,
  initialRequest
});

export const getTaxonomyVersionsOk = (versions, initialRequest) => ({
  type: GET_TAXONOMY_VERSIONS_OK,
  versions,
  initialRequest
});

export const getTaxonomyVersionsError = (error, initialRequest) => ({
  type: GET_TAXONOMY_VERSIONS_ERROR,
  error,
  initialRequest
});

export const importTaxonomyFile = (file) => ({
  type: IMPORT_TAXONOMY_FILE,
  file
});

export const importTaxonomyFileOk = () => ({
  type: IMPORT_TAXONOMY_FILE_OK
});

export const importTaxonomyFileError = (error) => ({
  type: IMPORT_TAXONOMY_FILE_ERROR,
  error
});

export const importTaxonomyCatalog = (scheme) => ({
  type: IMPORT_TAXONOMY_CATALOG,
  scheme
});

export const importTaxonomyCatalogOk = () => ({
  type: IMPORT_TAXONOMY_CATALOG_OK
});

export const importTaxonomyCatalogError = (error) => ({
  type: IMPORT_TAXONOMY_CATALOG_ERROR,
  error
});

export const copyFromCurrent = () => ({
  type: COPY_FROM_CURRENT
});

export const copyFromCurrentOk = () => ({
  type: COPY_FROM_CURRENT_OK
});

export const copyFromCurrentError = (error) => ({
  type: COPY_FROM_CURRENT_ERROR,
  error
});

export const calculateContentImpact = () => ({
  type: CALCULATE_CONTENT_IMPACT
});

export const calculateContentImpactOk = () => ({
  type: CALCULATE_CONTENT_IMPACT_OK
});

export const calculateContentImpactError = (error) => ({
  type: CALCULATE_CONTENT_IMPACT_ERROR,
  error
});

export const transitionToCurrent = () => ({
  type: TRANSITION_TO_CURRENT
});

export const transitionToCurrentOk = () => ({
  type: TRANSITION_TO_CURRENT_OK
});

export const transitionToCurrentError = (error) => ({
  type: TRANSITION_TO_CURRENT_ERROR,
  error
});

export const cancelTransitionToCurrent = () => ({
  type: CANCEL_TRANSITION_TO_CURRENT
});

export const cancelTransitionToCurrentOk = () => ({
  type: CANCEL_TRANSITION_TO_CURRENT_OK
});

export const cancelTransitionToCurrentError = (error) => ({
  type: CANCEL_TRANSITION_TO_CURRENT_ERROR,
  error
});

export const clear = () => ({
  type: CLEAR
});

export const clearOk = () => ({
  type: CLEAR_OK
});

export const clearError = (error) => ({
  type: CLEAR_ERROR,
  error
});

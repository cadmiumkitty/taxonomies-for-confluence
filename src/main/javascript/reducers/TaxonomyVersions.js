import _ from 'lodash';

import { GET_TAXONOMY_VERSIONS, GET_TAXONOMY_VERSIONS_OK, GET_TAXONOMY_VERSIONS_ERROR,
  IMPORT_TAXONOMY_FILE, IMPORT_TAXONOMY_FILE_OK, IMPORT_TAXONOMY_FILE_ERROR, 
  IMPORT_TAXONOMY_CATALOG, IMPORT_TAXONOMY_CATALOG_OK, IMPORT_TAXONOMY_CATALOG_ERROR, 
  COPY_FROM_CURRENT, COPY_FROM_CURRENT_OK, COPY_FROM_CURRENT_ERROR,
  CALCULATE_CONTENT_IMPACT, CALCULATE_CONTENT_IMPACT_OK, CALCULATE_CONTENT_IMPACT_ERROR,
  TRANSITION_TO_CURRENT, TRANSITION_TO_CURRENT_OK, TRANSITION_TO_CURRENT_ERROR,
  CANCEL_TRANSITION_TO_CURRENT, CANCEL_TRANSITION_TO_CURRENT_OK, CANCEL_TRANSITION_TO_CURRENT_ERROR,
  CLEAR, CLEAR_OK, CLEAR_ERROR } from '../actions/TaxonomyVersions';

export const taxonomyVersions = (state = {
  initialVersionsRequestStarted: false,
  subsequentVersionsRequestStarted: false,
  importTaxonomyFileRequestStarted: false,
  importTaxonomyCatalogRequestStarted: false,
  copyFromCurrentRequestStarted: false,
  calculateContentImpactRequestStarted: false,
  transitionToCurrentRequestStarted: false,
  cancelTransitionToCurrentRequestStarted: false,
  clearRequestStarted: false,
  versions: {}
}, action) => {
  const { initialRequest, versions } = action;
  switch (action.type) {
    case GET_TAXONOMY_VERSIONS:
      return {
        ...state,
        initialVersionsRequestStarted: true && initialRequest,
        subsequentVersionsRequestStarted: true && initialRequest,
      }
    case GET_TAXONOMY_VERSIONS_OK:
      return {
        ...state,
        initialVersionsRequestStarted: false,
        subsequentVersionsRequestStarted: false,
        versions
      }
    case GET_TAXONOMY_VERSIONS_ERROR:
      return {
        ...state,
        initialVersionsRequestStarted: false,
        subsequentVersionsRequestStarted: false,
      }
    case IMPORT_TAXONOMY_FILE:
      return {
        ...state,
        importTaxonomyFileRequestStarted: true
      };
    case IMPORT_TAXONOMY_FILE_OK:
      return {
        ...state,
        importTaxonomyFileRequestStarted: false
      };
    case IMPORT_TAXONOMY_FILE_ERROR:
      return {
        ...state,
        importTaxonomyFileRequestStarted: false
      };
    case IMPORT_TAXONOMY_CATALOG:
      return {
        ...state,
        importTaxonomyCatalogRequestStarted: true
      };
    case IMPORT_TAXONOMY_CATALOG_OK:
      return {
        ...state,
        importTaxonomyCatalogRequestStarted: false
      };
    case IMPORT_TAXONOMY_CATALOG_ERROR:
      return {
        ...state,
        importTaxonomyCatalogRequestStarted: false
      };
    case COPY_FROM_CURRENT:
      return {
        ...state,
        copyFromCurrentRequestStarted: true
      };
    case COPY_FROM_CURRENT_OK:
      return {
        ...state,
        copyFromCurrentRequestStarted: false
      };
    case COPY_FROM_CURRENT_ERROR:
      return {
        ...state,
        copyFromCurrentRequestStarted: false
      };
    case CALCULATE_CONTENT_IMPACT:
      return {
        ...state,
        calculateContentImpactRequestStarted: true
      };
    case CALCULATE_CONTENT_IMPACT_OK:
      return {
        ...state,
        calculateContentImpactRequestStarted: false
      };
    case CALCULATE_CONTENT_IMPACT_ERROR:
      return {
        ...state,
        calculateContentImpactRequestStarted: false
      };
    case TRANSITION_TO_CURRENT:
      return {
        ...state,
        transitionToCurrentRequestStarted: true
      };
    case TRANSITION_TO_CURRENT_OK:
      return {
        ...state,
        transitionToCurrentRequestStarted: false
      };
    case TRANSITION_TO_CURRENT_ERROR:
      return {
        ...state,
        transitionToCurrentRequestStarted: false
      };
    case CANCEL_TRANSITION_TO_CURRENT:
      return {
        ...state,
        cancelTransitionToCurrentRequestStarted: true
      };
    case CANCEL_TRANSITION_TO_CURRENT_OK:
      return {
        ...state,
        cancelTransitionToCurrentRequestStarted: false
      };
    case CANCEL_TRANSITION_TO_CURRENT_ERROR:
      return {
        ...state,
        cancelTransitionToCurrentRequestStarted: false
      };
    case CLEAR:
      return {
        ...state,
        clearRequestStarted: true
      };
    case CLEAR_OK:
      return {
        ...state,
        clearRequestStarted: false
      };
    case CLEAR_ERROR:
      return {
        ...state,
        clearRequestStarted: false
      };
    default:
      return state;
  }
}
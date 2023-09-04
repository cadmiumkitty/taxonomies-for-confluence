import _ from 'lodash';
import request from 'superagent';

import { GET_TAXONOMY_VERSIONS, IMPORT_TAXONOMY_FILE, IMPORT_TAXONOMY_CATALOG, COPY_FROM_CURRENT, CALCULATE_CONTENT_IMPACT, TRANSITION_TO_CURRENT, CANCEL_TRANSITION_TO_CURRENT, CLEAR } from '../actions/TaxonomyVersions';
import { getTaxonomyVersions, getTaxonomyVersionsOk, getTaxonomyVersionsError,
  importTaxonomyFileOk, importTaxonomyFileError,
  importTaxonomyCatalogOk, importTaxonomyCatalogError,
  copyFromCurrentOk, copyFromCurrentError,
  calculateContentImpactOk, calculateContentImpactError, 
  transitionToCurrentOk, transitionToCurrentError,
  cancelTransitionToCurrentOk, cancelTransitionToCurrentError, 
  clearOk, clearError } from '../actions/TaxonomyVersions';

export const createTaxonomyVersionsMiddleware = () => {

  const getTaxonomyVersionsRequest = (store, initialRequest) => token => {
    request
      .get(`/taxonomy/version`)
      .set('Authorization', `JWT ${token}`)
      .then(onGetTaxonomyVersionsResult(store, initialRequest))
      .catch(onGetTaxonomyVersionsError(store, initialRequest));
  }

  const onGetTaxonomyVersionsResult = (store, initialRequest) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getTaxonomyVersionsOk(entity, initialRequest));
  }

  const onGetTaxonomyVersionsError = (store, initialRequest) => error => {
    store.dispatch(getTaxonomyVersionsError(error, initialRequest));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Taxonomy version retrieval failed. ${error.message}`});
  }

  const importTaxonomyFileRequest = (store, file) => token => {
    request
      .post(`/taxonomy/version/draft/import-file`)
      .set('Authorization', `JWT ${token}`)
      .attach('file', file)
      .then(onImportTaxonomyFileResult(store))
      .catch(onImportTaxonomyFileError(store));
  }

  const onImportTaxonomyFileResult = (store) => result => {
    store.dispatch(importTaxonomyFileOk());
    store.dispatch(getTaxonomyVersions(false));
  }

  const onImportTaxonomyFileError = store => error => {
    store.dispatch(importTaxonomyFileError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Taxonomy file upload failed. ${error.message}`});
  }

  const importTaxonomyCatalogRequest = (store, scheme) => token => {
    const params = _.join(_.map(scheme, s => `scheme=${s}`), '&');
    request
      .post(`/taxonomy/version/draft/import-catalog`)
      .type('form')
      .set('Authorization', `JWT ${token}`)
      .send(params)
      .then(onImportTaxonomyCatalogResult(store))
      .catch(onImportTaxonomyCatalogError(store));
  }

  const onImportTaxonomyCatalogResult = (store) => result => {
    store.dispatch(importTaxonomyCatalogOk());
    store.dispatch(getTaxonomyVersions(false));
  }

  const onImportTaxonomyCatalogError = store => error => {
    store.dispatch(importTaxonomyCatalogError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Taxonomy catalog import request failed. ${error.message}`});
  }

  const copyFromCurrentRequest = (store) => token => {
    request
      .post(`/taxonomy/version/draft/copy-from-current`)
      .set('Authorization', `JWT ${token}`)
      .then(onCopyFromCurrentRequestResult(store))
      .catch(onCopyFromCurrentRequestError(store));
  }

  const onCopyFromCurrentRequestResult = (store) => result => {
    store.dispatch(copyFromCurrentOk());
    store.dispatch(getTaxonomyVersions(false));
  }

  const onCopyFromCurrentRequestError = store => error => {
    store.dispatch(copyFromCurrentError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Copy from current request failed. ${error.message}`});
  }

  const calculateContentImpactRequest = (store) => token => {
    request
      .post(`/taxonomy/version/draft/calculate-content-impact`)
      .set('Authorization', `JWT ${token}`)
      .then(onCalculateContentImpactRequestResult(store))
      .catch(onCalculateContentImpactRequestError(store));
  }

  const onCalculateContentImpactRequestResult = (store) => result => {
    store.dispatch(calculateContentImpactOk());
    store.dispatch(getTaxonomyVersions(false));
  }

  const onCalculateContentImpactRequestError = store => error => {
    store.dispatch(calculateContentImpactError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Version switch request failed. ${error.message}`});
  }

  const transitionToCurrentRequest = (store) => token => {
    request
      .post(`/taxonomy/version/draft/transition-to-current`)
      .set('Authorization', `JWT ${token}`)
      .then(onTransitionToCurrentRequestResult(store))
      .catch(onTransitionToCurrentRequestError(store));
  }

  const onTransitionToCurrentRequestResult = (store) => result => {
    store.dispatch(transitionToCurrentOk());
    store.dispatch(getTaxonomyVersions(false));
  }

  const onTransitionToCurrentRequestError = store => error => {
    store.dispatch(transitionToCurrentError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Version switch completion request failed. ${error.message}`});
  }

  const cancelTtransitionToCurrentRequest = (store) => token => {
    request
      .post(`/taxonomy/version/draft/cancel-transition-to-current`)
      .set('Authorization', `JWT ${token}`)
      .then(onCancelTransitionToCurrentRequestResult(store))
      .catch(onCancelTransitionToCurrentRequestError(store));
  }

  const onCancelTransitionToCurrentRequestResult = (store) => result => {
    store.dispatch(cancelTransitionToCurrentOk());
    store.dispatch(getTaxonomyVersions(false));
  }

  const onCancelTransitionToCurrentRequestError = store => error => {
    store.dispatch(cancelTransitionToCurrentError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Version switch cancellation request failed. ${error.message}`});
  }

  const clearRequest = (store) => token => {
    request
      .post(`/taxonomy/version/draft/clear`)
      .set('Authorization', `JWT ${token}`)
      .then(onClearRequestResult(store))
      .catch(onClearRequestError(store));
  }

  const onClearRequestResult = (store) => result => {
    store.dispatch(clearOk());
    store.dispatch(getTaxonomyVersions(false));
  }

  const onClearRequestError = store => error => {
    store.dispatch(clearError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Version clearing request failed. ${error.message}`});
  }

  return store => next => action => {
    switch (action.type) {
      case GET_TAXONOMY_VERSIONS:
        AP.context.getToken(getTaxonomyVersionsRequest(store, action.initialRequest));
        return next(action);
      case IMPORT_TAXONOMY_FILE:
        AP.context.getToken(importTaxonomyFileRequest(store, action.file));
        return next(action);
      case IMPORT_TAXONOMY_CATALOG:
        AP.context.getToken(importTaxonomyCatalogRequest(store, action.scheme));
        return next(action);
      case COPY_FROM_CURRENT:
        AP.context.getToken(copyFromCurrentRequest(store, action.scheme));
        return next(action);  
      case CALCULATE_CONTENT_IMPACT:
        AP.context.getToken(calculateContentImpactRequest(store));
        return next(action);
      case TRANSITION_TO_CURRENT:
        AP.context.getToken(transitionToCurrentRequest(store));
        return next(action);
      case CANCEL_TRANSITION_TO_CURRENT:
        AP.context.getToken(cancelTtransitionToCurrentRequest(store));
        return next(action);
      case CLEAR:
        AP.context.getToken(clearRequest(store));
        return next(action);
      default:
        return next(action);
    }
  }
}
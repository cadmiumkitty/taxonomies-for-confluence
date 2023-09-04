import _ from 'lodash';
import request from 'superagent';

import { GET_CONCEPT_SCHEMES, GET_TOP_CONCEPTS, GET_NARROWER_CONCEPTS, GET_CONTENT, GET_RESOURCE_PROVENANCE } from '../actions/Taxonomy';
import {
  getConceptSchemesOk, getConceptSchemesError,
  getTopConceptsOk, getTopConceptsError,
  getNarrowerConceptsOk, getNarrowerConceptsError,
  getContentOk, getContentError,
  getResourceProvenanceError, getResourceProvenanceOk } from '../actions/Taxonomy';

import { getId } from '../utils/Rdf';

export const createTaxonomyMiddleware = () => {

  const getConceptSchemesRequest = (store, context) => token => {
    request
      .get(`/taxonomy/conceptscheme`)
      .query({ context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetConceptSchemesResult(store, context))
      .catch(onGetConceptSchemesError(store, context));
  }

  const onGetConceptSchemesResult = (store, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getConceptSchemesOk(entity, context));
  }

  const onGetConceptSchemesError = (store, context) => error => {
    store.dispatch(getConceptSchemesError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Concept schemes retrieval failed. ${error.message}`});
  }

  const getTopConceptsRequest = (store, conceptSchemeUri, context) => token => {
    request
      .get('/taxonomy/topconcept')
      .query({ scheme: conceptSchemeUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetTopConceptsResult(store, conceptSchemeUri, context))
      .catch(onGetTopConceptsError(store, context));
  }

  const onGetTopConceptsResult = (store, conceptSchemeUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getTopConceptsOk(conceptSchemeUri, entity, context));
  }

  const onGetTopConceptsError = (store, context) => error => {
    store.dispatch(getTopConceptsError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Top concepts retrieval failed. ${error.message}`});
  }

  const getNarrowerConceptsRequest = (store, broaderConceptUri, context) => token => {
    request
      .get('/taxonomy/narrowerconcept')
      .query({ broader: broaderConceptUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetNarrowerConceptsResult(store, broaderConceptUri, context))
      .catch(onGetNarrowerConceptsResultError(store, context));
  }

  const onGetNarrowerConceptsResult = (store, broaderConceptUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getNarrowerConceptsOk(broaderConceptUri, entity, context));
  }

  const onGetNarrowerConceptsResultError = (store, context) => error => {
    store.dispatch(getNarrowerConceptsError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Narrower concepts retrieval failed. ${error.message}`});
  }

  const getContentRequest = (store, subjectUri) => token => {
    request
      .get('/taxonomy/content')
      .query({ concept: subjectUri })
      .set('Authorization', `JWT ${token}`)
      .then(onGetContentResult(store, subjectUri))
      .catch(onGetContentError(store));
  }

  const onGetContentResult = (store, subjectUri) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getContentOk(subjectUri, entity));
  }

  const onGetContentError = store => error => {
    store.dispatch(getContentError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Retrieval of content failed. ${error.message}`});
  }

  const getResourceProvenanceRequest = (store, resource) => token => {
    request
      .get('/provenance')
      .query({ resource: getId(resource) })
      .set('Authorization', `JWT ${token}`)
      .then(onGetResourceProvenanceResult(store))
      .catch(onGetResourceProvenanceError(store));
  }

  const onGetResourceProvenanceResult = (store) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getResourceProvenanceOk(entity));
  }

  const onGetResourceProvenanceError = store => error => {
    store.dispatch(getResourceProvenanceError(error));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Retrieval of provenance failed. ${error.message}`});
  }

  return store => next => action => {
    switch (action.type) {
      case GET_CONCEPT_SCHEMES:
        AP.context.getToken(getConceptSchemesRequest(store, action.context));
        return next(action);
      case GET_TOP_CONCEPTS:
        AP.context.getToken(getTopConceptsRequest(store, action.conceptSchemeUri, action.context));
        return next(action);
      case GET_NARROWER_CONCEPTS:
        AP.context.getToken(getNarrowerConceptsRequest(store, action.broaderConceptUri, action.context));
        return next(action);
      case GET_CONTENT:
        AP.context.getToken(getContentRequest(store, action.subjectUri));
        return next(action);
      case GET_RESOURCE_PROVENANCE:
        AP.context.getToken(getResourceProvenanceRequest(store, action.resource));
        return next(action);  
      default:
        return next(action);
    }
  }
}
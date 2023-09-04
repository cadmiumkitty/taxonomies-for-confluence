import _ from 'lodash';

import request from 'superagent';

import { GET_CONCEPT, QUERY_CONCEPTS } from '../actions/Concept';
import { getConceptOk, getConceptError, queryConceptsOk, queryConceptsError } from '../actions/Concept';

export const createConceptMiddleware = () => {

  const getConceptRequest = (store, uri, context) => token => {
    request
      .get('/taxonomy/concept')
      .query({ uri: uri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetConceptResult(store, uri, context))
      .catch(onGetConceptError(store, uri, context));
  }

  const onGetConceptResult = (store, uri, context) => result => {
    var results = JSON.parse(result.text)
    if (_.size(results) > 0) {
      store.dispatch(getConceptOk(uri, results, context));
    } else {
      store.dispatch(getConceptError(uri, { message: 'No concept found.' }, context));      
    }
  }

  const onGetConceptError = (store, uri, context) => error => {
    store.dispatch(getConceptError(uri, error, context));
  }

  const queryConceptsRequest = (store, q) => token => {
    request
      .get('/taxonomy/search')
      .query({ q: q })
      .set('Authorization', `JWT ${token}`)
      .then(onQueryConceptsResult(store))
      .catch(onQueryConceptsError(store));    
  }

  const onQueryConceptsResult = store => result => {
    var results = JSON.parse(result.text)
    store.dispatch(queryConceptsOk(results));
  }

  const onQueryConceptsError = store => error => {
    store.dispatch(queryConceptsError(error));
  }

  return store => next => action => {
    switch (action.type) {
      case GET_CONCEPT:
        AP.context.getToken(getConceptRequest(store, action.uri, action.context));
        return next(action);
      case QUERY_CONCEPTS:
        AP.context.getToken(queryConceptsRequest(store, action.q));
        return next(action);
      default:
        return next(action);
    }
  }
}
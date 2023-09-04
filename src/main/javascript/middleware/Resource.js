import _ from 'lodash';

import request from 'superagent';

import { GET_RESOURCE, QUERY_RESOURCES } from '../actions/Resource';
import { getResourceOk, getResourceError, queryResourcesOk, queryResourcesError } from '../actions/Resource';

export const createResourceMiddleware = () => {

  const getResourceRequest = (store, uri, context) => token => {
    request
      .get('/schema/resource')
      .query({ uri: uri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetResourceResult(store, uri, context))
      .catch(onGetResourceError(store, uri, context));
  }

  const onGetResourceResult = (store, uri, context) => result => {
    var results = JSON.parse(result.text)
    if (_.size(results) > 0) {
      store.dispatch(getResourceOk(uri, results, context));
    } else {
      store.dispatch(getResourceError(uri, { message: 'No concept found.' }, context));      
    }
  }

  const onGetResourceError = (store, uri, context) => error => {
    store.dispatch(getResourceError(uri, error, context));
  }

  const queryResourcesRequest = (store, q) => token => {
    request
      .get('/schema/search')
      .query({ q: q })
      .set('Authorization', `JWT ${token}`)
      .then(onQueryResourcesResult(store))
      .catch(onQueryResourcesError(store));    
  }

  const onQueryResourcesResult = store => result => {
    var results = JSON.parse(result.text)
    store.dispatch(queryResourcesOk(results));
  }

  const onQueryResourcesError = store => error => {
    store.dispatch(queryResourcesError(error));
  }

  return store => next => action => {
    switch (action.type) {
      case GET_RESOURCE:
        AP.context.getToken(getResourceRequest(store, action.uri, action.context));
        return next(action);
      case QUERY_RESOURCES:
        AP.context.getToken(queryResourcesRequest(store, action.q));
        return next(action);
      default:
        return next(action);
    }
  }
}
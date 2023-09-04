import _ from 'lodash';

import request from 'superagent';

import { QUERY_RDFS_CLASSES } from '../actions/RdfsClass';
import { getRdfsClassOk, getRdfsClassError, queryRdfsClassesOk, queryRdfsClassesError } from '../actions/RdfsClass';

export const createRdfsClassMiddleware = () => {

  const queryRdfsClassesRequest = (store, q) => token => {
    request
      .get('/class/search')
      .query({ q: q })
      .set('Authorization', `JWT ${token}`)
      .then(onQueryRdfsClassesResult(store))
      .catch(onQueryRdfsClassesError(store));    
  }

  const onQueryRdfsClassesResult = store => result => {
    var results = JSON.parse(result.text)
    store.dispatch(queryRdfsClassesOk(results));
  }

  const onQueryRdfsClassesError = store => error => {
    store.dispatch(queryRdfsClassesError(error));
  }

  return store => next => action => {
    switch (action.type) {
      case QUERY_RDFS_CLASSES:
        AP.context.getToken(queryRdfsClassesRequest(store, action.q));
        return next(action);
      default:
        return next(action);
    }
  }
}
import _ from 'lodash';
import request from 'superagent';

import { 
  GET_DEFINING_RESOURCES_FOR_PROPERTIES, GET_DEFINING_RESOURCES_FOR_PROPERTIES_OK, GET_DEFINING_RESOURCES_FOR_PROPERTIES_ERROR,
  GET_TOP_PROPERTIES, GET_TOP_PROPERTIES_OK, GET_TOP_PROPERTIES_ERROR,
  GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE, GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_OK, GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR,
  GET_SUB_PROPERTIES, GET_SUB_PROPERTIES_OK, GET_SUB_PROPERTIES_ERROR,
  GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE, GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_OK, GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR,
  GET_CONTENT_WITH_PROPERTY_AS_PREDICATE, GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_OK, GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_ERROR } from '../actions/SchemaProperty';
import {
  getDefiningResourcesForProperties, getDefiningResourcesForPropertiesOk, getDefiningResourcesForPropertiesError,
  getTopProperties, getTopPropertiesOk, getTopPropertiesError,
  getTopPropertiesForDefiningResource, getTopPropertiesForDefiningResourceOk, getTopPropertiesForDefiningResourceError,
  getSubProperties, getSubPropertiesOk, getSubPropertiesError, 
  getSubPropertiesForDefiningResource, getSubPropertiesForDefiningResourceOk, getSubPropertiesForDefiningResourceError, 
  getContentWithPropertyAsPredicate, getContentWithPropertyAsPredicateOk, getContentWithPropertyAsPredicateError } from '../actions/SchemaProperty';

export const createSchemaPropertyMiddleware = () => {

  const getDefiningResourcesForPropertiesRequest = (store, context) => token => {
    request
      .get(`/schema/defining-resources-for-properties`)
      .query({ context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetDefiningResourcesForPropertiesResult(store, context))
      .catch(onGetDefiningResourcesForPropertiesError(store, context));
  }

  const onGetDefiningResourcesForPropertiesResult = (store, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getDefiningResourcesForPropertiesOk(entity, context));
  }

  const onGetDefiningResourcesForPropertiesError = (store, context) => error => {
    store.dispatch(getDefiningResourcesForPropertiesError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Defining resources retrieval failed. ${error.message}`});
  }

  const getTopPropertiesRequest = (store, context) => token => {
    request
      .get('/schema/top-properties')
      .query({ context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetTopPropertiesResult(store, context))
      .catch(onGetTopPropertiesError(store, context));
  }

  const onGetTopPropertiesResult = (store, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getTopPropertiesOk(entity, context));
  }

  const onGetTopPropertiesError = (store, context) => error => {
    store.dispatch(getTopPropertiesError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Top Properties retrieval failed for Properties with no defining resources. ${error.message}`});
  }

  const getTopPropertiesForDefiningResourceRequest = (store, definingResourceUri, context) => token => {
    request
      .get('/schema/top-properties-for-defining-resource')
      .query({ resource: definingResourceUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetTopPropertiesForDefiningResourceResult(store, definingResourceUri, context))
      .catch(onGetTopPropertiesForDefiningResourceError(store, context));
  }

  const onGetTopPropertiesForDefiningResourceResult = (store, definingResourceUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getTopPropertiesForDefiningResourceOk(definingResourceUri, entity, context));
  }

  const onGetTopPropertiesForDefiningResourceError = (store, context) => error => {
    store.dispatch(getTopPropertiesForDefiningResourceError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Top Properties retrieval failed. ${error.message}`});
  }

  const getSubPropertiesRequest = (store, propertyUri, context) => token => {
    request
      .get('/schema/sub-properties')
      .query({ property: propertyUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetSubPropertiesResult(store, propertyUri, context))
      .catch(onGetSubPropertiesError(store, context));
  }

  const onGetSubPropertiesResult = (store, propertyUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getSubPropertiesOk(propertyUri, entity, context));
  }

  const onGetSubPropertiesError = (store, context) => error => {
    store.dispatch(getSubPropertiesError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `SubProperties retrieval failed. ${error.message}`});
  }

  const getSubPropertiesForDefiningResourceRequest = (store, definingResourceUri, propertyUri, context) => token => {
    request
      .get('/schema/sub-properties-for-defining-resource')
      .query({ resource: definingResourceUri, property: propertyUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetSubPropertiesForDefiningResourceResult(store, definingResourceUri, propertyUri, context))
      .catch(onGetSubPropertiesForDefiningResourceError(store, context));
  }

  const onGetSubPropertiesForDefiningResourceResult = (store, definingResourceUri, propertyUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getSubPropertiesForDefiningResourceOk(definingResourceUri, propertyUri, entity, context));
  }

  const onGetSubPropertiesForDefiningResourceError = (store, context) => error => {
    store.dispatch(getSubPropertiesForDefiningResourceError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `SubProperties retrieval failed. ${error.message}`});
  }

  const getContentWithPropertyAsPredicateRequest = (store, propertyUri, context) => token => {
    request
      .get('/schema/content-with-property-as-predicate')
      .query({ property: propertyUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(getContentWithPropertyAsPredicateRequestResult(store, propertyUri, context))
      .catch(getContentWithPropertyAsPredicateRequestError(store, context));
  }

  const getContentWithPropertyAsPredicateRequestResult = (store, propertyUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getContentWithPropertyAsPredicateOk(propertyUri, entity, context));
  }

  const getContentWithPropertyAsPredicateRequestError = (store, context) => error => {
    store.dispatch(getContentWithPropertyAsPredicateError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Retrieval of content where property is a predicate failed. ${error.message}`});
  }

  return store => next => action => {
    switch (action.type) {
      case GET_DEFINING_RESOURCES_FOR_PROPERTIES:
        AP.context.getToken(getDefiningResourcesForPropertiesRequest(store, action.context))
        return next(action);
      case GET_TOP_PROPERTIES:
        AP.context.getToken(getTopPropertiesRequest(store, action.context));
        return next(action);
      case GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE:
        AP.context.getToken(getTopPropertiesForDefiningResourceRequest(store, action.definingResourceUri, action.context))
        return next(action);
      case GET_SUB_PROPERTIES:
        AP.context.getToken(getSubPropertiesRequest(store, action.propertyUri, action.context));
        return next(action);
      case GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE:
        AP.context.getToken(getSubPropertiesForDefiningResourceRequest(store, action.definingResourceUri, action.propertyUri, action.context));
        return next(action);
      case GET_CONTENT_WITH_PROPERTY_AS_PREDICATE:
        AP.context.getToken(getContentWithPropertyAsPredicateRequest(store, action.propertyUri, action.context))
        return next(action);  
      default:
        return next(action);
    }
  }
}
import _ from 'lodash';
import request from 'superagent';

import { 
  GET_DEFINING_RESOURCES_FOR_CLASSES, GET_DEFINING_RESOURCES_FOR_CLASSES_OK, GET_DEFINING_RESOURCES_FOR_CLASSES_ERROR,
  GET_TOP_CLASSES, GET_TOP_CLASSES_OK, GET_TOP_CLASSES_ERROR,
  GET_TOP_CLASSES_FOR_DEFINING_RESOURCE, GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_OK, GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_ERROR,
  GET_SUB_CLASSES, GET_SUB_CLASSES_OK, GET_SUB_CLASSES_ERROR,
  GET_SUB_CLASSES_FOR_DEFINING_RESOURCE, GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_OK, GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_ERROR,
  GET_CONTENT_WITH_CLASS_AS_OBJECT, GET_CONTENT_WITH_CLASS_AS_OBJECT_OK, GET_CONTENT_WITH_CLASS_AS_OBJECT_ERROR } from '../actions/SchemaClass';
import {
  getDefiningResourcesForClasses, getDefiningResourcesForClassesOk, getDefiningResourcesForClassesError,
  getTopClasses, getTopClassesOk, getTopClassesError,
  getTopClassesForDefiningResource, getTopClassesForDefiningResourceOk, getTopClassesForDefiningResourceError,
  getSubClasses, getSubClassesOk, getSubClassesError, 
  getSubClassesForDefiningResource, getSubClassesForDefiningResourceOk, getSubClassesForDefiningResourceError, 
  getContentWithClassAsObject, getContentWithClassAsObjectOk, getContentWithClassAsObjectError } from '../actions/SchemaClass';

export const createSchemaClassMiddleware = () => {

  const getDefiningResourcesForClassesRequest = (store, context) => token => {
    request
      .get(`/schema/defining-resources-for-classes`)
      .query({ context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetDefiningResourcesForClassesResult(store, context))
      .catch(onGetDefiningResourcesForClassesError(store, context));
  }

  const onGetDefiningResourcesForClassesResult = (store, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getDefiningResourcesForClassesOk(entity, context));
  }

  const onGetDefiningResourcesForClassesError = (store, context) => error => {
    store.dispatch(getDefiningResourcesForClassesError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Defining resources retrieval failed. ${error.message}`});
  }

  const getTopClassesRequest = (store, context) => token => {
    request
      .get('/schema/top-classes')
      .query({ context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetTopClassesResult(store, context))
      .catch(onGetTopClassesError(store, context));
  }

  const onGetTopClassesResult = (store, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getTopClassesOk(entity, context));
  }

  const onGetTopClassesError = (store, context) => error => {
    store.dispatch(getTopClassesError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Top classes retrieval failed for classes with no defining resources. ${error.message}`});
  }

  const getTopClassesForDefiningResourceRequest = (store, definingResourceUri, context) => token => {
    request
      .get('/schema/top-classes-for-defining-resource')
      .query({ resource: definingResourceUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetTopClassesForDefiningResourceResult(store, definingResourceUri, context))
      .catch(onGetTopClassesForDefiningResourceError(store, context));
  }

  const onGetTopClassesForDefiningResourceResult = (store, definingResourceUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getTopClassesForDefiningResourceOk(definingResourceUri, entity, context));
  }

  const onGetTopClassesForDefiningResourceError = (store, context) => error => {
    store.dispatch(getTopClassesForDefiningResourceError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Top classes retrieval failed. ${error.message}`});
  }

  const getSubClassesRequest = (store, classUri, context) => token => {
    request
      .get('/schema/sub-classes')
      .query({ class: classUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetSubClassesResult(store, classUri, context))
      .catch(onGetSubClassesError(store, context));
  }

  const onGetSubClassesResult = (store, classUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getSubClassesOk(classUri, entity, context));
  }

  const onGetSubClassesError = (store, context) => error => {
    store.dispatch(getSubClassesError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Subclasses retrieval failed. ${error.message}`});
  }

  const getSubClassesForDefiningResourceRequest = (store, definingResourceUri, classUri, context) => token => {
    request
      .get('/schema/sub-classes-for-defining-resource')
      .query({ resource: definingResourceUri, class: classUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetSubClassesForDefiningResourceResult(store, definingResourceUri, classUri, context))
      .catch(onGetSubClassesForDefiningResourceError(store, context));
  }

  const onGetSubClassesForDefiningResourceResult = (store, definingResourceUri, classUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getSubClassesForDefiningResourceOk(definingResourceUri, classUri, entity, context));
  }

  const onGetSubClassesForDefiningResourceError = (store, context) => error => {
    store.dispatch(getSubClassesForDefiningResourceError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Subclasses retrieval failed. ${error.message}`});
  }

  const getContentWithClassAsObjectRequest = (store, classUri, context) => token => {
    request
      .get('/schema/content-with-class-as-object')
      .query({ class: classUri, context: context })
      .set('Authorization', `JWT ${token}`)
      .then(onGetContentWithClassAsObjectResult(store, classUri, context))
      .catch(onGetContentWithClassAsObjectError(store, context));
  }

  const onGetContentWithClassAsObjectResult = (store, classUri, context) => result => {
    var entity = JSON.parse(result.text)
    store.dispatch(getContentWithClassAsObjectOk(classUri, entity, context));
  }

  const onGetContentWithClassAsObjectError = (store, context) => error => {
    store.dispatch(getContentWithClassAsObjectError(error, context));
    AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Retrieval of content where class is an object failed. ${error.message}`});
  }

  return store => next => action => {
    switch (action.type) {
      case GET_DEFINING_RESOURCES_FOR_CLASSES:
        AP.context.getToken(getDefiningResourcesForClassesRequest(store, action.context))
        return next(action);
      case GET_TOP_CLASSES:
        AP.context.getToken(getTopClassesRequest(store, action.context));
        return next(action);
      case GET_TOP_CLASSES_FOR_DEFINING_RESOURCE:
        AP.context.getToken(getTopClassesForDefiningResourceRequest(store, action.definingResourceUri, action.context))
        return next(action);
      case GET_SUB_CLASSES:
        AP.context.getToken(getSubClassesRequest(store, action.classUri, action.context));
        return next(action);
      case GET_SUB_CLASSES_FOR_DEFINING_RESOURCE:
        AP.context.getToken(getSubClassesForDefiningResourceRequest(store, action.definingResourceUri, action.classUri, action.context));
        return next(action);
      case GET_CONTENT_WITH_CLASS_AS_OBJECT:
        AP.context.getToken(getContentWithClassAsObjectRequest(store, action.classUri, action.context))
        return next(action);  
      default:
        return next(action);
    }
  }
}
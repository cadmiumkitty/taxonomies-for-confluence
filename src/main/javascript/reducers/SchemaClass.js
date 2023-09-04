import _ from 'lodash';

import { 
  GET_DEFINING_RESOURCES_FOR_CLASSES, GET_DEFINING_RESOURCES_FOR_CLASSES_OK, GET_DEFINING_RESOURCES_FOR_CLASSES_ERROR,
  GET_TOP_CLASSES, GET_TOP_CLASSES_OK, GET_TOP_CLASSES_ERROR,
  GET_TOP_CLASSES_FOR_DEFINING_RESOURCE, GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_OK, GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_ERROR,
  GET_SUB_CLASSES, GET_SUB_CLASSES_OK, GET_SUB_CLASSES_ERROR,
  GET_SUB_CLASSES_FOR_DEFINING_RESOURCE, GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_OK, GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_ERROR,
  GET_CONTENT_WITH_CLASS_AS_OBJECT, GET_CONTENT_WITH_CLASS_AS_OBJECT_OK, GET_CONTENT_WITH_CLASS_AS_OBJECT_ERROR } from '../actions/SchemaClass';

import { getId } from '../utils/Rdf';
import { getClassCount, isBlankDefiningResource } from '../utils/Team';

export const schemaClass = (state = {
  emptyDefiningResourceForClassesRequestStarted: false,
  definingResourceForClassesRequestStarted: false,
  topClassesRequestStarted: false,
  topClassesForDefiningResourceRequestStarted: false,
  subClassesRequestStarted: false,
  subClassesForDefiningResourceRequestStarted: false,
  contentWithClassAsObject: false,
  emptyDefiningResourcesUri: undefined,
  definingResources: [],
  classes: [],
  context: undefined
}, action) => {
  const { definingResources, 
    definingResourceUri, topClasses,
    classUri, subClasses, content,
    context } = action;
  switch (action.type) {

    case GET_DEFINING_RESOURCES_FOR_CLASSES:
      return {
        ...state,
        definingResourceForClassesRequestStarted: true
      };
    case GET_DEFINING_RESOURCES_FOR_CLASSES_OK:
      const emptyDefiningResourcesUri = getId(_.head(_.filter(definingResources, isBlankDefiningResource)));
      const drc = _.sortBy(_.filter(definingResources, element => { return getClassCount(element) > 0; }), element => { return isBlankDefiningResource(element) ? 1 : 0 ; });
      return {
        ...state,
        definingResourceForClassesRequestStarted: false,
        definingResources: drc,
        emptyDefiningResourcesUri,
        classes: [],
        context
      };
    case GET_DEFINING_RESOURCES_FOR_CLASSES_ERROR:
      return {
        ...state,
        definingResourceForClassesRequestStarted: false,
        definingResources,
        classes: []
      };

    case GET_TOP_CLASSES:
      return {
        ...state,
        topClassesRequestStarted: true
      };
    case GET_TOP_CLASSES_OK:
      // Top classes that have no defining resource can simply be put under emptyDefiningResourcesUri
      // that we got in the earlier interaction with the service.
      // We only do this for display and are not using this blank resource identifier in any calls
      const tc = {};
      tc[state.emptyDefiningResourcesUri] = topClasses;
      return {
        ...state,
        topClassesRequestStarted: false,
        classes: _.assign({}, state.classes, tc)
      };
    case GET_TOP_CLASSES_ERROR:
      return {
        ...state,
        topClassesRequestStarted: false
      };

    case GET_TOP_CLASSES_FOR_DEFINING_RESOURCE:
      return {
        ...state,
        topClassesForDefiningResourceRequestStarted: true
      };
    case GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_OK:
      const tcdr = {};
      tcdr[definingResourceUri] = topClasses;
      return {
        ...state,
        topClassesForDefiningResourceRequestStarted: false,
        classes: _.assign({}, state.classes, tcdr)
      };
    case GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_ERROR:
      return {
        ...state,
        topClassesForDefiningResourceRequestStarted: false
      };

    case GET_SUB_CLASSES:
      return {
        ...state,
        subClassesRequestStarted: true
      };
    case GET_SUB_CLASSES_OK:
      const sc = {};
      sc[classUri] = _.sortBy(_.uniqBy(_.concat(_.compact(state.classes[classUri]), subClasses), '@id'), element => { 
        return _.find(element['@type'], x => _.startsWith(x, 'http://www.w3.org/2000/01/rdf-schema#')) ? 0 : 1;
      });
      return {
        ...state,
        subClassesRequestStarted: false,
        classes: _.assign({}, state.classes, sc)
      };
    case GET_SUB_CLASSES_ERROR:
      return {
        ...state,
        subClassesRequestStarted: false
      };

      case GET_SUB_CLASSES_FOR_DEFINING_RESOURCE:
        return {
          ...state,
          subClassesForDefiningResourceRequestStarted: true
        };
      case GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_OK:
        const scfdr = {};
        scfdr[classUri] = _.sortBy(_.uniqBy(_.concat(_.compact(state.classes[classUri]), subClasses), '@id'), element => { 
          return _.find(element['@type'], x => _.startsWith(x, 'http://www.w3.org/2000/01/rdf-schema#')) ? 0 : 1;
        });
        return {
          ...state,
          subClassesForDefiningResourceRequestStarted: false,
          classes: _.assign({}, state.classes, scfdr)
        };
      case GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_ERROR:
        return {
          ...state,
          subClassesForDefiningResourceRequestStarted: false
        };        

    case GET_CONTENT_WITH_CLASS_AS_OBJECT:
      return {
        ...state,
        contentWithClassAsObjectRequestStarted: true
      };
    case GET_CONTENT_WITH_CLASS_AS_OBJECT_OK:
      const c = {};
      c[classUri] = _.sortBy(_.uniqBy(_.concat(_.compact(state.classes[classUri]), content), '@id'), element => { 
        return _.find(element['@type'], x => _.startsWith(x, 'http://www.w3.org/2000/01/rdf-schema#')) ? 0 : 1;
      });
      return {
        ...state,
        contentWithClassAsObjectRequestStarted: false,
        classes: _.assign({}, state.classes, c)
      };
    case GET_CONTENT_WITH_CLASS_AS_OBJECT_ERROR:
      return {
        ...state,
        contentWithClassAsObjectRequestStarted: false
      };  
        
    default:
      return state;
  }
}
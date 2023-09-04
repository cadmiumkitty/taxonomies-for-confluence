import _ from 'lodash';

import { 
  GET_DEFINING_RESOURCES_FOR_PROPERTIES, GET_DEFINING_RESOURCES_FOR_PROPERTIES_OK, GET_DEFINING_RESOURCES_FOR_PROPERTIES_ERROR,
  GET_TOP_PROPERTIES, GET_TOP_PROPERTIES_OK, GET_TOP_PROPERTIES_ERROR,
  GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE, GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_OK, GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR,
  GET_SUB_PROPERTIES, GET_SUB_PROPERTIES_OK, GET_SUB_PROPERTIES_ERROR,
  GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE, GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_OK, GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR,
  GET_CONTENT_WITH_PROPERTY_AS_PREDICATE, GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_OK, GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_ERROR } from '../actions/SchemaProperty';

import { getId } from '../utils/Rdf';
import { getPropertyCount, isBlankDefiningResource } from '../utils/Team';

export const schemaProperty = (state = {
  emptyDefiningResourceForPropertiesRequestStarted: false,
  definingResourceForPropertiesRequestStarted: false,
  topPropertiesRequestStarted: false,
  topPropertiesForDefiningResourceRequestStarted: false,
  subPropertiesRequestStarted: false,
  subPropertiesForDefiningResourceRequestStarted: false,
  contentWithPropertyAsPredicateRequestStarted: false,
  emptyDefiningResourcesUri: undefined,
  definingResources: [],
  properties: [],
  context: undefined
}, action) => {
  const { definingResources, 
    definingResourceUri, topProperties,
    propertyUri, subProperties, content,
    context } = action;
  switch (action.type) {

    case GET_DEFINING_RESOURCES_FOR_PROPERTIES:
      return {
        ...state,
        definingResourceForPropertiesRequestStarted: true
      };
    case GET_DEFINING_RESOURCES_FOR_PROPERTIES_OK:
      const emptyDefiningResourcesUri = getId(_.head(_.filter(definingResources, isBlankDefiningResource)));
      const drp = _.sortBy(_.filter(definingResources, element => { return getPropertyCount(element) > 0; }), element => { return isBlankDefiningResource(element) ? 1 : 0 ; });
      return {
        ...state,
        definingResourceForPropertiesRequestStarted: false,
        definingResources: drp,
        emptyDefiningResourcesUri,
        properties: [],
        context
      };
    case GET_DEFINING_RESOURCES_FOR_PROPERTIES_ERROR:
      return {
        ...state,
        definingResourceForPropertiesRequestStarted: false,
        definingResources,
        properties: []
      };

    case GET_TOP_PROPERTIES:
      return {
        ...state,
        topPropertiesRequestStarted: true
      };
    case GET_TOP_PROPERTIES_OK:
      // Top Properties that have no defining resource can simply be put under emptyDefiningResourcesUri
      // that we got in the earlier interaction with the service.
      // We only do this for display and are not using this blank resource identifier in any calls
      const tp = {};
      tp[state.emptyDefiningResourcesUri] = topProperties;
      return {
        ...state,
        topPropertiesRequestStarted: false,
        properties: _.assign({}, state.properties, tp)
      };
    case GET_TOP_PROPERTIES_ERROR:
      return {
        ...state,
        topPropertiesRequestStarted: false
      };

    case GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE:
      return {
        ...state,
        topPropertiesForDefiningResourceRequestStarted: true
      };
    case GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_OK:
      const tpdr = {};
      tpdr[definingResourceUri] = topProperties;
      return {
        ...state,
        topPropertiesForDefiningResourceRequestStarted: false,
        properties: _.assign({}, state.properties, tpdr)
      };
    case GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR:
      return {
        ...state,
        topPropertiesForDefiningResourceRequestStarted: false
      };

    case GET_SUB_PROPERTIES:
      return {
        ...state,
        subPropertiesRequestStarted: true
      };
    case GET_SUB_PROPERTIES_OK:
      const sp = {};
      sp[propertyUri] = _.sortBy(_.uniqBy(_.concat(_.compact(state.properties[propertyUri]), subProperties), '@id'), element => { 
        return _.find(element['@type'], x => _.startsWith(x, 'http://www.w3.org/2000/01/rdf-schema#')) ? 0 : 1;
      });
      return {
        ...state,
        subPropertiesRequestStarted: false,
        properties: _.assign({}, state.properties, sp)
      };
    case GET_SUB_PROPERTIES_ERROR:
      return {
        ...state,
        subPropertiesRequestStarted: false
      };

      case GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE:
        return {
          ...state,
          subPropertiesForDefiningResourceRequestStarted: true
        };
      case GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_OK:
        const spfdr = {};
        spfdr[propertyUri] = _.sortBy(_.uniqBy(_.concat(_.compact(state.properties[propertyUri]), subProperties), '@id'), element => { 
          return _.find(element['@type'], x => _.startsWith(x, 'http://www.w3.org/2000/01/rdf-schema#')) ? 0 : 1;
        });
        return {
          ...state,
          subPropertiesForDefiningResourceRequestStarted: false,
          properties: _.assign({}, state.properties, spfdr)
        };
      case GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR:
        return {
          ...state,
          subPropertiesForDefiningResourceRequestStarted: false
        };        

    case GET_CONTENT_WITH_PROPERTY_AS_PREDICATE:
      return {
        ...state,
        contentWithPropertyAsPredicateRequestStarted: true
      };
    case GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_OK:
      const c = {};
      c[propertyUri] = _.sortBy(_.uniqBy(_.concat(_.compact(state.properties[propertyUri]), content), '@id'), element => { 
        return _.find(element['@type'], x => _.startsWith(x, 'http://www.w3.org/2000/01/rdf-schema#')) ? 0 : 1;
      });
      return {
        ...state,
        contentWithPropertyAsPredicateRequestStarted: false,
        properties: _.assign({}, state.properties, c)
      };
    case GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_ERROR:
      return {
        ...state,
        contentWithPropertyAsPredicateRequestStarted: false
      };  
        
    default:
      return state;
  }
}
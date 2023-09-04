import _ from 'lodash';

import { 
  GET_CONCEPT_SCHEMES, GET_CONCEPT_SCHEMES_OK, GET_CONCEPT_SCHEMES_ERROR,
  GET_TOP_CONCEPTS, GET_TOP_CONCEPTS_OK, GET_TOP_CONCEPTS_ERROR,
  GET_NARROWER_CONCEPTS, GET_NARROWER_CONCEPTS_OK, GET_NARROWER_CONCEPTS_ERROR,
  GET_CONTENT, GET_CONTENT_OK, GET_CONTENT_ERROR,
  GET_RESOURCE_PROVENANCE, GET_RESOURCE_PROVENANCE_OK, GET_RESOURCE_PROVENANCE_ERROR } from '../actions/Taxonomy';

export const taxonomy = (state = {
  uploadTaxonomyRequestSarted: false,
  conceptSchemesRequestStarted: false,
  topConceptsRequestStarted: false,
  narrowerConceptsRequestStarted: false,
  conceptSchemes: [],
  concepts: [],
  context: undefined
}, action) => {
  const { conceptSchemes, 
    conceptSchemeUri, topConcepts, 
    broaderConceptUri, narrowerConcepts, 
    subjectUri, content,
    resource, provenance, 
    context } = action;

  switch (action.type) {
    case GET_CONCEPT_SCHEMES:
      return {
        ...state,
        conceptSchemesRequestStarted: true
      };
    case GET_CONCEPT_SCHEMES_OK:
      return {
        ...state,
        conceptSchemesRequestStarted: false,
        conceptSchemes,
        concepts: [],
        context
      };
    case GET_CONCEPT_SCHEMES_ERROR:
      return {
        ...state,
        conceptSchemesRequestStarted: false,
        conceptSchemes,
        concepts: []
      };

    case GET_TOP_CONCEPTS:
      return {
        ...state,
        topConceptsRequestStarted: true
      };
    case GET_TOP_CONCEPTS_OK:
      const tc = {};
      tc[conceptSchemeUri] = topConcepts;
      return {
        ...state,
        topConceptRequestStarted: false,
        concepts: _.assign({}, state.concepts, tc)
      };
    case GET_TOP_CONCEPTS_ERROR:
      return {
        ...state,
        topConceptsRequestStarted: false,
      };

    case GET_NARROWER_CONCEPTS:
      return {
        ...state,
        narrowerConceptsRequestStarted: true
      };
    case GET_NARROWER_CONCEPTS_OK:
      const nc = {};
      nc[broaderConceptUri] = _.sortBy(_.uniqBy(_.concat(_.compact(state.concepts[broaderConceptUri]), narrowerConcepts), '@id'), element => { 
        return _.find(element['@type'], x => _.startsWith(x, 'http://www.w3.org/2004/02/skos/core#')) ? 0 : 1;
      });
      return {
        ...state,
        narrowerConceptsRequestStarted: false,
        concepts: _.assign({}, state.concepts, nc)
      };
    case GET_NARROWER_CONCEPTS_ERROR:
      return {
        ...state,
        narrowerConceptsRequestStarted: false,
      };
      
    case GET_CONTENT:
      return {
        ...state,
        contentRequestStarted: true
      };
      case GET_CONTENT_OK:
      const c = {};
      c[subjectUri] = _.sortBy(_.uniqBy(_.concat(_.compact(state.concepts[subjectUri]), content), '@id'), element => { 
        return _.find(element['@type'], x => _.startsWith(x, 'http://www.w3.org/2004/02/skos/core#')) ? 0 : 1;
      });
      return {
        ...state,
        contentRequestStarted: false,
        concepts: _.assign({}, state.concepts, c)
      };
    case GET_CONTENT_ERROR:
      return {
        ...state,
        contentRequestStarted: false,
      };

    case GET_RESOURCE_PROVENANCE:
      return {
        ...state,
        provenanceRequestStarted: true,
        resource: resource
      };
    case GET_RESOURCE_PROVENANCE_OK:
      return {
        ...state,
        provenanceRequestStarted: false,
        provenance: provenance
      };
    case GET_RESOURCE_PROVENANCE_ERROR:
      return {
        ...state,
        provenanceRequestStarted: false,
      };

    default:
      return state;
  }
}
import _ from 'lodash';

import { GET_PARAMETERS_OK } from '../actions/Macro';
import { GET_CONCEPT_OK, GET_CONCEPT_ERROR } from '../actions/Concept';
import { SET_RELATION } from '../actions/RelationMacro';

import { isConcept, isConceptScheme } from '../utils/Skos';

export const relationMacro = (state = {
  relationMacroStartingUp: true
}, action) => {
  switch (action.type) {
    case GET_PARAMETERS_OK:
      if (_.isEmpty(action.parameters)) {
        return {
          ...state,
          relationMacroStartingUp: false
        };
      } else {
        return {
          ...state,
          relationMacroStartingUp: true,
          uri: action.parameters.uri
        };
      }
    case GET_CONCEPT_OK:
      return {
        ...state,
        relationMacroStartingUp: false,
        uri: action.uri,
        concept: _.head(_.filter(action.results, isConcept)),
        scheme: _.head(_.filter(action.results, isConceptScheme))
      };
    case GET_CONCEPT_ERROR:
      return {
        ...state,
        relationMacroStartingUp: false,
        error: action.error
      };
    case SET_RELATION:
      const { uri, concept, scheme } = action;
      return {
        ...state,
        uri,
        concept,
        scheme
      };
    default:
      return state;
  }
}
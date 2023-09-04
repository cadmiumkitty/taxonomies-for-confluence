import _ from 'lodash';

import { GET_PARAMETERS_OK } from '../actions/Macro';
import { GET_CONCEPT_OK, GET_CONCEPT_ERROR } from '../actions/Concept';
import { SET_TRANSITIVE, SET_PREDICATE, SET_OBJECT, ADD_PREDICATE_OBJECT, DELETE_PREDICATE_OBJECT } from '../actions/TocMacro';

import { isConcept, isConceptScheme } from '../utils/Skos';

export const toc = (state = {
  tocMacroStartingUp: true,
  transitive: false, 
  predicateUri: 'http://purl.org/dc/terms/relation',
  predicateObject: []
}, action) => {
  switch (action.type) {
    case GET_PARAMETERS_OK:
      if (_.isEmpty(action.parameters)) {
        return {
          ...state,
          tocMacroStartingUp: false
        };
      } else {
        const splitTransitive = _.split(action.parameters.transitive, ',');
        const splitPredicate = _.split(action.parameters.predicate, ',');
        const splitObject = _.split(action.parameters.object, ',');
        return {
          ...state,
          predicateObject: _.zipWith(splitTransitive, splitPredicate, splitObject, (t, p, o) => { return { transitive: t == 'true', predicateUri: p, objectUri: o }; })
        };  
      }
    case GET_CONCEPT_OK:
      const predicateObjectUpdatedOnConceptOk = _.map(state.predicateObject, po => {
        if (po.objectUri == action.uri) {
          return {
            ...po,
            objectConcept: _.head(_.filter(action.results, isConcept)),
            objectScheme: _.head(_.filter(action.results, isConceptScheme))
          }
        } else {
          return po;
        }
      })
      return {
        ...state,
        predicateObject: predicateObjectUpdatedOnConceptOk,
        tocMacroStartingUp: !_.every(predicateObjectUpdatedOnConceptOk, po => { return (po.objectUri && po.objectConcept) || po.error; })
      };
    case GET_CONCEPT_ERROR:
      const predicateObjectUpdatedOnConceptError = _.map(state.predicateObject, po => {
        if (po.objectUri == action.uri) {
          return {
            ...po,
            error: action.error
          }
        } else {
          return po;
        }
      })
      return {
        ...state,
        predicateObject: predicateObjectUpdatedOnConceptError,
        tocMacroStartingUp: !_.every(predicateObjectUpdatedOnConceptError, po => { return (po.objectUri && po.objectConcept) || po.error; })
      };
    case SET_TRANSITIVE:
      return {
        ...state,
        transitive: action.transitive
      };
    case SET_PREDICATE:
      return {
        ...state,
        predicateUri: action.uri
      };
    case SET_OBJECT:
      return {
        ...state,
        objectUri: action.uri,
        objectConcept: action.concept,
        objectScheme: action.scheme
      };
    case ADD_PREDICATE_OBJECT:
      const predicateObjectUpdatedOnAdd = _.concat(state.predicateObject, 
        {
          transitive: state.transitive,
          predicateUri: state.predicateUri,
          objectUri: state.objectUri,
          objectConcept: state.objectConcept,
          objectScheme: state.objectScheme
        });
      return {
        tocMacroStartingUp: false,
        transitive: false, 
        predicateUri: 'http://purl.org/dc/terms/relation',    
        predicateObject: predicateObjectUpdatedOnAdd
      };

    case DELETE_PREDICATE_OBJECT:
      const head = action.index == 0 ? [] : _.slice(state.predicateObject, 0, action.index);
      const tail = action.index == _.size(state.predicateObject) ? [] : _.slice(state.predicateObject, action.index + 1);
      const predicateObjectUpdatedOnDelete = _.concat(head, tail);
      return {
        ...state,
        predicateObject: predicateObjectUpdatedOnDelete
      };
    default:
      return state;
  }
}
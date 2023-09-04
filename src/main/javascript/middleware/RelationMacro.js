import _ from 'lodash';

import { GET_PARAMETERS_OK } from '../actions/Macro';
import { saveMacro } from '../actions/Macro';
import { getConcept } from '../actions/Concept';
import { RELATION_MACRO_SAVE_MACRO } from '../actions/RelationMacro';

import { getId } from '../utils/Rdf';
import { getPrefLabel, getNotation, getAltLabel } from '../utils/Skos';

export const createRelationMacroMiddleware = () => {

  return store => next => action => {
    switch (action.type) {
      case GET_PARAMETERS_OK:
        if (!_.isEmpty(action.parameters)) {
          store.dispatch(getConcept(action.parameters.uri));
        }
        return next(action);
      case RELATION_MACRO_SAVE_MACRO:
        const relatedConcept = store.getState().relationMacro.concept;
        const uri = getId(relatedConcept);
        const prefLabel = getPrefLabel(relatedConcept);
        const altLabel = getAltLabel(relatedConcept);
        const notation = getNotation(relatedConcept);
        const parameters = {
          uri,
          prefLabel,
          altLabel: _.isEmpty(altLabel) ? undefined : _.join(altLabel, ','),
          notation: _.isEmpty(notation) ? undefined : _.join(notation, ',')
        }
        store.dispatch(saveMacro(parameters, prefLabel))
        return next(action);
      default:
        return next(action);
    }
  }
}
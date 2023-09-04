import _ from 'lodash';

import { saveMacro } from '../actions/Macro';
import { SPARQL_MACRO_SAVE_MACRO } from '../actions/SparqlMacro';

export const createSparqlMacroMiddleware = () => {

  return store => next => action => {
    switch (action.type) {
      case SPARQL_MACRO_SAVE_MACRO:
        const parameters = {
          query: store.getState().sparql.query
        }
        store.dispatch(saveMacro(parameters, ''));
        return next(action);
      default:
        return next(action);
    }
  }
}
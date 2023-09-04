import _ from 'lodash';

import { GET_PARAMETERS_OK } from '../actions/Macro';
import { saveMacro } from '../actions/Macro';
import { getConcept } from '../actions/Concept';
import { TOC_MACRO_SAVE_MACRO } from '../actions/TocMacro';

export const createTocMacroMiddleware = () => {

  return store => next => action => {
    switch (action.type) {
      case GET_PARAMETERS_OK:
        if (!_.isEmpty(action.parameters)) {
          _.forEach(_.split(action.parameters.object, ','), objectUri => store.dispatch(getConcept(objectUri)));
        }
        return next(action);
      case TOC_MACRO_SAVE_MACRO:
        const parameters = {
          transitive: _.join(_.map(store.getState().toc.predicateObject, po => po.transitive), ','),
          predicate: _.join(_.map(store.getState().toc.predicateObject, po => po.predicateUri), ','),
          object: _.join(_.map(store.getState().toc.predicateObject, po => po.objectUri), ',')
        }
        store.dispatch(saveMacro(parameters, ''));
        return next(action);
      default:
        return next(action);
    }
  }
}
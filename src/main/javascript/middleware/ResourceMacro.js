import _ from 'lodash';

import { GET_PARAMETERS_OK } from '../actions/Macro';
import { saveMacro } from '../actions/Macro';
import { getResource } from '../actions/Resource';
import { RESOURCE_MACRO_SAVE_MACRO } from '../actions/ResourceMacro';

import { getId } from '../utils/Rdf';
import { getLabel } from '../utils/Label';
import { getLabel as getRdfsLabel } from '../utils/Rdfs';
import { getPrefLabel as getSkosPrefLabel } from '../utils/Skos';

export const createResourceMacroMiddleware = () => {

  return store => next => action => {
    switch (action.type) {
      case GET_PARAMETERS_OK:
        if (!_.isEmpty(action.parameters)) {
          store.dispatch(getResource(action.parameters.uri));
        }
        return next(action);
      case RESOURCE_MACRO_SAVE_MACRO:
        const resource = store.getState().resourceMacro.resource;
        const uri = getId(resource);
        const label = getRdfsLabel(resource);
        const prefLabel= getSkosPrefLabel(resource);
        const body = getLabel(resource);
        const parameters = {
          uri,
          label: _.isEmpty(label) ? undefined : label,
          prefLabel: _.isEmpty(prefLabel) ? undefined : prefLabel
        }
        store.dispatch(saveMacro(parameters, body))
        return next(action);
      default:
        return next(action);
    }
  }
}
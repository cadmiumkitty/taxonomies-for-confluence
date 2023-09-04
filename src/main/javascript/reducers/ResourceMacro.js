import _ from 'lodash';

import { GET_PARAMETERS_OK } from '../actions/Macro';
import { GET_RESOURCE_OK, GET_RESOURCE_ERROR } from '../actions/Resource';
import { SET_RESOURCE } from '../actions/ResourceMacro';
import { getIsDefinedBy } from '../utils/Rdfs';

export const resourceMacro = (state = {
  resourceMacroStartingUp: true
}, action) => {
  switch (action.type) {
    case GET_PARAMETERS_OK:
      if (_.isEmpty(action.parameters)) {
        return {
          ...state,
          resourceMacroStartingUp: false
        };
      } else {
        return {
          ...state,
          resourceMacroStartingUp: true,
          uri: action.parameters.uri
        };
      }
    case GET_RESOURCE_OK:
      const r = _.head(_.filter(action.results, r => r['@id'] == action.uri));
      const idbUri = getIsDefinedBy(r);
      const idb = _.head(_.filter(action.results, r => r['@id'] == idbUri));
      return {
        ...state,
        resourceMacroStartingUp: false,
        uri: action.uri,
        resource: r,
        isDefinedBy: idb
      };
    case GET_RESOURCE_ERROR:
      return {
        ...state,
        resourceMacroStartingUp: false,
        error: action.error
      };
    case SET_RESOURCE:
      const { uri, resource } = action;
      return {
        ...state,
        uri,
        resource
      };
    default:
      return state;
  }
}
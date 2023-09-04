import _ from 'lodash';

import { GET_PARAMETERS_OK } from '../actions/Macro';
import { SET_SPARQL_QUERY } from '../actions/SparqlMacro';

export const sparql = (state = {
  query: ''
}, action) => {
  switch (action.type) {
    case GET_PARAMETERS_OK:
      if (_.isEmpty(action.parameters)) {
        return {
          ...state
        };
      } else {
        const query = action.parameters.query;
        return {
          ...state,
          query
        };  
      }
    case SET_SPARQL_QUERY:
      const query = action.query;
      return {
        ...state,
        query
      };
    default:
      return state;
  }
}
import { GET_PARAMETERS_OK, GET_BODY_OK } from '../actions/Macro';

export const macro = (state = {
}, action) => {
  const { parameters, body } = action;
  switch (action.type) {
    case GET_PARAMETERS_OK:
      return {
        ...state,
        parameters
      };
    case GET_BODY_OK:
      return {
        ...state,
        body
      };  
    default:
      return state;
  }
}
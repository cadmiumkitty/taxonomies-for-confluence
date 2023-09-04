import { GET_CONTENT_PROPERTY_OK, SET_CONTENT_PROPERTY_OK } from '../actions/Content';

export const content = (state = {
  contentPropertyReady: false,
  properties: {}
}, action) => {
  const { property } = action;
  switch (action.type) {
    case GET_CONTENT_PROPERTY_OK:
      return {
        ...state,
        contentPropertyReady: true,
        properties: _.assign({}, state.properties, property ? { [property.key]: property } : {})
      };
    case SET_CONTENT_PROPERTY_OK:
      return {
        ...state,
        properties: _.assign({}, state.properties, { [property.key]: property })
      };
    default:
      return state;
  }
}
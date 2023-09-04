import { GET_CONTENT_PROPERTY, SET_CONTENT_PROPERTY } from '../actions/Content';
import { getContentPropertyOk, setContentPropertyOk, setContentPropertyError } from '../actions/Content';

export const createContentMiddleware = () => {

  const onGetContentPropertyResult = store => result => {
    store.dispatch(getContentPropertyOk(result));
  }

  const onSetContentPropertyResult = store => result => {
    if (result && typeof result.error == 'undefined') {
      store.dispatch(setContentPropertyOk(result));
      AP.flag.create({type: 'success', close: 'auto', title: 'Success', body: 'Property has been set.'});
    } else {
      store.dispatch(setContentPropertyError(result.error));
      AP.flag.create({type: 'error', close: 'auto', title: 'Error', body: `Property setting failed. ${result.error}`});
    }
  }

  return store => next => action => {
    switch (action.type) {
      case GET_CONTENT_PROPERTY:
        AP.confluence.getContentProperty(action.key, onGetContentPropertyResult(store));
        return next(action);
      case SET_CONTENT_PROPERTY:
        AP.confluence.setContentProperty(action.property, onSetContentPropertyResult(store));
        return next(action);
      default:
        return next(action);
    }
  }
}
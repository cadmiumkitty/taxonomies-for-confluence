import { GET_PARAMETERS, GET_BODY, SAVE_MACRO } from '../actions/Macro';
import { getParametersOk, getBodyOk } from '../actions/Macro';

export const createMacroMiddleware = () => {

  const onGetMacroDataResult = store => parameters => {
    store.dispatch(getParametersOk(parameters));
  }

  const onGetMacroBodyResult = store => body => {
    store.dispatch(getBodyOk(body));
  }

  return store => next => action => {
    switch (action.type) {
      case GET_PARAMETERS:
        AP.confluence.getMacroData(onGetMacroDataResult(store));
        return next(action);
      case GET_BODY:
        AP.confluence.getMacroBody(onGetMacroBodyResult(store));
        return next(action);
      case SAVE_MACRO:
        AP.confluence.saveMacro(action.parameters, action.body);
        return next(action);
      default:
        return next(action);
    }
  }
}
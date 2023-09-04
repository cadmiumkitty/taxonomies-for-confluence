export const SET_RESOURCE = 'SET_RESOURCE';
export const RESOURCE_MACRO_SAVE_MACRO = 'RESOURCE_MACRO_SAVE_MACRO';

export const setResource = (uri, resource) => ({
  type: SET_RESOURCE,
  uri,
  resource
});

export const resourceMacroSaveMacro = () => ({
  type: RESOURCE_MACRO_SAVE_MACRO
});

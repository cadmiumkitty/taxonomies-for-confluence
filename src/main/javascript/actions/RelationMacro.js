export const SET_RELATION = 'SET_RELATION';
export const RELATION_MACRO_SAVE_MACRO = 'RELATION_MACRO_SAVE_MACRO';

export const setRelation = (uri, concept, scheme) => ({
  type: SET_RELATION,
  uri,
  concept,
  scheme
});

export const relationMacroSaveMacro = () => ({
  type: RELATION_MACRO_SAVE_MACRO
});

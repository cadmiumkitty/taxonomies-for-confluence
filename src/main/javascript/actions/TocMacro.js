export const SET_TRANSITIVE = 'SET_TRANSITIVE';
export const SET_PREDICATE = 'SET_PREDICATE';
export const SET_OBJECT = 'SET_OBJECT';
export const ADD_PREDICATE_OBJECT = 'ADD_PREDICATE_OBJECT';
export const DELETE_PREDICATE_OBJECT = 'DELETE_PREDICATE_OBJECT';
export const TOC_MACRO_SAVE_MACRO = 'TOC_MACRO_SAVE_MACRO';

export const setTransitive = (transitive) => ({
  type: SET_TRANSITIVE,
  transitive
});

export const setPredicate = (uri) => ({
  type: SET_PREDICATE,
  uri
});

export const setObject = (uri, concept, scheme) => ({
  type: SET_OBJECT,
  uri,
  concept,
  scheme
});

export const addPredicateObject = (index) => ({
  type: ADD_PREDICATE_OBJECT
});

export const deletePredicateObject = (index) => ({
  type: DELETE_PREDICATE_OBJECT,
  index
});

export const tocMacroSaveMacro = () => ({
  type: TOC_MACRO_SAVE_MACRO
});

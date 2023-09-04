export const SPARQL_MACRO_SAVE_MACRO = 'SPARQL_MACRO_SAVE_MACRO';
export const SET_SPARQL_QUERY = 'SET_SPARQL_QUERY';

export const sparqlMacroSaveMacro = () => ({
  type: SPARQL_MACRO_SAVE_MACRO
});

export const setSparqlQuery = (query) => ({
  type: SET_SPARQL_QUERY,
  query
});

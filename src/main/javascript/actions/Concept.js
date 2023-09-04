export const GET_CONCEPT = 'GET_CONCEPT';
export const GET_CONCEPT_OK = 'GET_CONCEPT_OK';
export const GET_CONCEPT_ERROR = 'GET_CONCEPT_ERROR';

export const QUERY_CONCEPTS = 'QUERY_CONCEPTS';
export const QUERY_CONCEPTS_OK = 'QUERY_CONCEPTS_OK';
export const QUERY_CONCEPTS_ERROR = 'QUERY_CONCEPTS_ERROR';

export const RESET_CONCEPTS_QUERY = 'RESET_CONCEPTS_QUERY';

export const getConcept = (uri, context) => ({
  type: GET_CONCEPT,
  uri,
  context
});

export const getConceptOk = (uri, results, context) => ({
  type: GET_CONCEPT_OK,
  uri,
  results,
  context
});

export const getConceptError = (uri, error, context) => ({
  type: GET_CONCEPT_ERROR,
  uri,
  error,
  context
});

export const queryConcepts = (q) => ({
  type: QUERY_CONCEPTS,
  q
});

export const queryConceptsOk = (results) => ({
  type: QUERY_CONCEPTS_OK,
  results
});

export const queryConceptsError = (error) => ({
  type: QUERY_CONCEPTS_ERROR,
  error
});

export const resetConceptsQuery = () => ({
  type: RESET_CONCEPTS_QUERY
});

export const GET_CONCEPT_SCHEMES = 'GET_CONCEPT_SCHEMES';
export const GET_CONCEPT_SCHEMES_OK = 'GET_CONCEPT_SCHEMES_OK';
export const GET_CONCEPT_SCHEMES_ERROR = 'GET_CONCEPT_SCHEMES_ERROR';

export const GET_TOP_CONCEPTS = 'GET_TOP_CONCEPTS';
export const GET_TOP_CONCEPTS_OK = 'GET_TOP_CONCEPTS_OK';
export const GET_TOP_CONCEPTS_ERROR = 'GET_TOP_CONCEPTS_ERROR';

export const GET_NARROWER_CONCEPTS = 'GET_NARROWER_CONCEPTS';
export const GET_NARROWER_CONCEPTS_OK = 'GET_NARROWER_CONCEPTS_OK';
export const GET_NARROWER_CONCEPTS_ERROR = 'GET_NARROWER_CONCEPTS_ERROR';

export const GET_CONTENT = 'GET_CONTENT';
export const GET_CONTENT_OK = 'GET_CONTENT_OK';
export const GET_CONTENT_ERROR = 'GET_CONTENT_ERROR';

export const GET_RESOURCE_PROVENANCE = 'GET_RESOURCE_PROVENANCE';
export const GET_RESOURCE_PROVENANCE_OK = 'GET_RESOURCE_PROVENANCE_OK';
export const GET_RESOURCE_PROVENANCE_ERROR = 'GET_RESOURCE_PROVENANCE_ERROR';

export const getConceptSchemes = (context) => ({
  type: GET_CONCEPT_SCHEMES,
  context
});

export const getConceptSchemesOk = (conceptSchemes, context) => ({
  type: GET_CONCEPT_SCHEMES_OK,
  conceptSchemes,
  context
});

export const getConceptSchemesError = (error, context) => ({
  type: GET_CONCEPT_SCHEMES_ERROR,
  error,
  context
});

export const getTopConcepts = (conceptSchemeUri, context) => ({
  type: GET_TOP_CONCEPTS,
  conceptSchemeUri,
  context
});

export const getTopConceptsOk = (conceptSchemeUri, topConcepts, context) => ({
  type: GET_TOP_CONCEPTS_OK,
  conceptSchemeUri,
  topConcepts,
  context
});

export const getTopConceptsError = (error, context) => ({
  type: GET_TOP_CONCEPTS_ERROR,
  error,
  context
});

export const getNarrowerConcepts = (broaderConceptUri, context) => ({
  type: GET_NARROWER_CONCEPTS,
  broaderConceptUri,
  context
});

export const getNarrowerConceptsOk = (broaderConceptUri, narrowerConcepts, context) => ({
  type: GET_NARROWER_CONCEPTS_OK,
  broaderConceptUri,
  narrowerConcepts,
  context
});

export const getNarrowerConceptsError = (error, context) => ({
  type: GET_NARROWER_CONCEPTS_ERROR,
  error,
  context
});

export const getContent = (subjectUri) => ({
  type: GET_CONTENT,
  subjectUri
});

export const getContentOk = (subjectUri, content) => ({
  type: GET_CONTENT_OK,
  subjectUri,
  content
});

export const getContentError = (error) => ({
  type: GET_CONTENT_ERROR,
  error
});

export const getResourceProvenance = (resource) => ({
  type: GET_RESOURCE_PROVENANCE,
  resource
});

export const getResourceProvenanceOk = (provenance) => ({
  type: GET_RESOURCE_PROVENANCE_OK,
  provenance
});

export const getResourceProvenanceError = (error) => ({
  type: GET_RESOURCE_PROVENANCE_ERROR,
  error
});
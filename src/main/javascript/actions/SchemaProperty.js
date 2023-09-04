export const GET_DEFINING_RESOURCES_FOR_PROPERTIES = 'GET_DEFINING_RESOURCES_FOR_PROPERTIES';
export const GET_DEFINING_RESOURCES_FOR_PROPERTIES_OK = 'GET_DEFINING_RESOURCES_FOR_PROPERTIES_OK';
export const GET_DEFINING_RESOURCES_FOR_PROPERTIES_ERROR = 'GET_DEFINING_RESOURCES_FOR_PROPERTIES_ERROR';

export const GET_TOP_PROPERTIES = 'GET_TOP_PROPERTIES';
export const GET_TOP_PROPERTIES_OK = 'GET_TOP_PROPERTIES_OK';
export const GET_TOP_PROPERTIES_ERROR = 'GET_TOP_PROPERTIES_ERROR';

export const GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE = 'GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE';
export const GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_OK = 'GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_OK';
export const GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR = 'GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR';

export const GET_SUB_PROPERTIES = 'GET_SUB_PROPERTIES';
export const GET_SUB_PROPERTIES_OK = 'GET_SUB_PROPERTIES_OK';
export const GET_SUB_PROPERTIES_ERROR = 'GET_SUB_PROPERTIES_ERROR';

export const GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE = 'GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE';
export const GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_OK = 'GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_OK';
export const GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR = 'GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR';

export const GET_CONTENT_WITH_PROPERTY_AS_PREDICATE = 'GET_CONTENT_WITH_PROPERTY_AS_PREDICATE';
export const GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_OK = 'GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_OK';
export const GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_ERROR = 'GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_ERROR';

export const getDefiningResourcesForProperties = (context) => ({
  type: GET_DEFINING_RESOURCES_FOR_PROPERTIES,
  context
});
export const getDefiningResourcesForPropertiesOk = (definingResources, context) => ({
  type: GET_DEFINING_RESOURCES_FOR_PROPERTIES_OK,
  definingResources,
  context
});
export const getDefiningResourcesForPropertiesError = (context) => ({
  type: GET_DEFINING_RESOURCES_FOR_PROPERTIES_ERROR,
  context
});

export const getTopProperties = (context) => ({
  type: GET_TOP_PROPERTIES,
  context
});
export const getTopPropertiesOk = (topProperties, context) => ({
  type: GET_TOP_PROPERTIES_OK,
  topProperties,
  context
});
export const getTopPropertiesError = (error, context) => ({
  type: GET_TOP_PROPERTIES_ERROR,
  error,
  context
});

export const getTopPropertiesForDefiningResource = (definingResourceUri, context) => ({
  type: GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE,
  definingResourceUri,
  context
});
export const getTopPropertiesForDefiningResourceOk = (definingResourceUri, topProperties, context) => ({
  type: GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_OK,
  definingResourceUri,
  topProperties,
  context
});
export const getTopPropertiesForDefiningResourceError = (error, context) => ({
  type: GET_TOP_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR,
  error,
  context
});

export const getSubProperties = (propertyUri, context) => ({
  type: GET_SUB_PROPERTIES,
  propertyUri,
  context
});
export const getSubPropertiesOk = (propertyUri, subProperties, context) => ({
  type: GET_SUB_PROPERTIES_OK,
  propertyUri,
  subProperties,
  context
});
export const getSubPropertiesError = (error, context) => ({
  type: GET_SUB_PROPERTIES_ERROR,
  error,
  context
});

export const getSubPropertiesForDefiningResource = (definingResourceUri, propertyUri, context) => ({
  type: GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE,
  definingResourceUri,
  propertyUri,
  context
});
export const getSubPropertiesForDefiningResourceOk = (definingResourceUri, propertyUri, subProperties, context) => ({
  type: GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_OK,
  definingResourceUri,
  propertyUri,
  subProperties,
  context
});
export const getSubPropertiesForDefiningResourceError = (error, context) => ({
  type: GET_SUB_PROPERTIES_FOR_DEFINING_RESOURCE_ERROR,
  error,
  context
});

export const getContentWithPropertyAsPredicate = (propertyUri, context) => ({
  type: GET_CONTENT_WITH_PROPERTY_AS_PREDICATE,
  propertyUri,
  context
});
export const getContentWithPropertyAsPredicateOk = (propertyUri, content, context) => ({
  type: GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_OK,
  propertyUri,
  content,
  context
});
export const getContentWithPropertyAsPredicateError = (error, context) => ({
  type: GET_CONTENT_WITH_PROPERTY_AS_PREDICATE_ERROR,
  error,
  context
});

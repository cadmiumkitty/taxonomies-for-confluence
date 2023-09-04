export const GET_DEFINING_RESOURCES_FOR_CLASSES = 'GET_DEFINING_RESOURCES_FOR_CLASSES';
export const GET_DEFINING_RESOURCES_FOR_CLASSES_OK = 'GET_DEFINING_RESOURCES_FOR_CLASSES_OK';
export const GET_DEFINING_RESOURCES_FOR_CLASSES_ERROR = 'GET_DEFINING_RESOURCES_FOR_CLASSES_ERROR';

export const GET_TOP_CLASSES = 'GET_TOP_CLASSES';
export const GET_TOP_CLASSES_OK = 'GET_TOP_CLASSES_OK';
export const GET_TOP_CLASSES_ERROR = 'GET_TOP_CLASSES_ERROR';

export const GET_TOP_CLASSES_FOR_DEFINING_RESOURCE = 'GET_TOP_CLASSES_FOR_DEFINING_RESOURCE';
export const GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_OK = 'GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_OK';
export const GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_ERROR = 'GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_ERROR';

export const GET_SUB_CLASSES = 'GET_SUB_CLASSES';
export const GET_SUB_CLASSES_OK = 'GET_SUB_CLASSES_OK';
export const GET_SUB_CLASSES_ERROR = 'GET_SUB_CLASSES_ERROR';

export const GET_SUB_CLASSES_FOR_DEFINING_RESOURCE = 'GET_SUB_CLASSES_FOR_DEFINING_RESOURCE';
export const GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_OK = 'GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_OK';
export const GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_ERROR = 'GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_ERROR';

export const GET_CONTENT_WITH_CLASS_AS_OBJECT = 'GET_CONTENT_WITH_CLASS_AS_OBJECT';
export const GET_CONTENT_WITH_CLASS_AS_OBJECT_OK = 'GET_CONTENT_WITH_CLASS_AS_OBJECT_OK';
export const GET_CONTENT_WITH_CLASS_AS_OBJECT_ERROR = 'GET_CONTENT_WITH_CLASS_AS_OBJECT_ERROR';

export const getDefiningResourcesForClasses = (context) => ({
  type: GET_DEFINING_RESOURCES_FOR_CLASSES,
  context
});
export const getDefiningResourcesForClassesOk = (definingResources, context) => ({
  type: GET_DEFINING_RESOURCES_FOR_CLASSES_OK,
  definingResources,
  context
});
export const getDefiningResourcesForClassesError = (context) => ({
  type: GET_DEFINING_RESOURCES_FOR_CLASSES_ERROR,
  context
});

export const getTopClasses = (context) => ({
  type: GET_TOP_CLASSES,
  context
});
export const getTopClassesOk = (topClasses, context) => ({
  type: GET_TOP_CLASSES_OK,
  topClasses,
  context
});
export const getTopClassesError = (error, context) => ({
  type: GET_TOP_CLASSES_ERROR,
  error,
  context
});

export const getTopClassesForDefiningResource = (definingResourceUri, context) => ({
  type: GET_TOP_CLASSES_FOR_DEFINING_RESOURCE,
  definingResourceUri,
  context
});
export const getTopClassesForDefiningResourceOk = (definingResourceUri, topClasses, context) => ({
  type: GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_OK,
  definingResourceUri,
  topClasses,
  context
});
export const getTopClassesForDefiningResourceError = (error, context) => ({
  type: GET_TOP_CLASSES_FOR_DEFINING_RESOURCE_ERROR,
  error,
  context
});

export const getSubClasses = (classUri, context) => ({
  type: GET_SUB_CLASSES,
  classUri,
  context
});
export const getSubClassesOk = (classUri, subClasses, context) => ({
  type: GET_SUB_CLASSES_OK,
  classUri,
  subClasses,
  context
});
export const getSubClassesError = (error, context) => ({
  type: GET_SUB_CLASSES_ERROR,
  error,
  context
});

export const getSubClassesForDefiningResource = (definingResourceUri, classUri, context) => ({
  type: GET_SUB_CLASSES_FOR_DEFINING_RESOURCE,
  definingResourceUri,
  classUri,
  context
});
export const getSubClassesForDefiningResourceOk = (definingResourceUri, classUri, subClasses, context) => ({
  type: GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_OK,
  definingResourceUri,
  classUri,
  subClasses,
  context
});
export const getSubClassesForDefiningResourceError = (error, context) => ({
  type: GET_SUB_CLASSES_FOR_DEFINING_RESOURCE_ERROR,
  error,
  context
});

export const getContentWithClassAsObject = (classUri, context) => ({
  type: GET_CONTENT_WITH_CLASS_AS_OBJECT,
  classUri,
  context
});
export const getContentWithClassAsObjectOk = (classUri, content, context) => ({
  type: GET_CONTENT_WITH_CLASS_AS_OBJECT_OK,
  classUri,
  content,
  context
});
export const getContentWithClassAsObjectError = (error, context) => ({
  type: GET_CONTENT_WITH_CLASS_AS_OBJECT_ERROR,
  error,
  context
});

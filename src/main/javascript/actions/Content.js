export const GET_CONTENT_PROPERTY = 'GET_CONTENT_PROPERTY';
export const GET_CONTENT_PROPERTY_OK = 'GET_CONTENT_PROPERTY_OK';

export const SET_CONTENT_PROPERTY = 'SET_CONTENT_PROPERTY';
export const SET_CONTENT_PROPERTY_OK = 'SET_CONTENT_PROPERTY_OK';
export const SET_CONTENT_PROPERTY_ERROR = 'SET_CONTENT_PROPERTY_ERROR';

export const getContentProperty = (key) => ({
  type: GET_CONTENT_PROPERTY,
  key
});

export const getContentPropertyOk = (property) => ({
  type: GET_CONTENT_PROPERTY_OK,
  property
});

export const setContentProperty = (property) => ({
  type: SET_CONTENT_PROPERTY,
  property
});

export const setContentPropertyOk = (property) => ({
  type: SET_CONTENT_PROPERTY_OK,
  property
});

export const setContentPropertyError = (error) => ({
  type: SET_CONTENT_PROPERTY_ERROR,
  error
});

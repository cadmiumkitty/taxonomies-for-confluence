export const GET_PARAMETERS = 'GET_PARAMETERS';
export const GET_PARAMETERS_OK = 'GET_PARAMETERS_OK';
export const GET_BODY = 'GET_BODY';
export const GET_BODY_OK = 'GET_BODY_OK';

export const SAVE_MACRO = 'SAVE_MACRO';

export const getParameters = () => ({
  type: GET_PARAMETERS
});

export const getParametersOk = (parameters) => ({
  type: GET_PARAMETERS_OK,
  parameters
});

export const getBody = () => ({
  type: GET_BODY
});

export const getBodyOk = (body) => ({
  type: GET_BODY_OK,
  body
});

export const saveMacro = (parameters, body) => ({
  type: SAVE_MACRO,
  parameters,
  body
});
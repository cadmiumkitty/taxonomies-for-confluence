import _ from 'lodash';

export const getId = (resource) => {
  return resource['@id'];
}

export const getType = (resource) => {
  return resource['@type'];
}

export const isProperty = (resource) => {
  return _.includes(getType(resource), 'http://www.w3.org/1999/02/22-rdf-syntax-ns#Property');  
}
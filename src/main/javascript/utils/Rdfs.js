import _ from 'lodash';

import { getType } from './Rdf';

export const getLabel = (resource) => {
  return _.head(_.map(_.filter(resource['http://www.w3.org/2000/01/rdf-schema#label'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));
}

export const getIsDefinedBy = (resource) => {
  return _.head(_.map(resource['http://www.w3.org/2000/01/rdf-schema#isDefinedBy'], r => r['@id']));
}

export const isClass = (resource) => {
  const types = _.map(resource['http://www.w3.org/1999/02/22-rdf-syntax-ns#type'], r => r['@id']);
  const otherTypes = getType(resource); 
  return _.includes(types, 'http://www.w3.org/2000/01/rdf-schema#Class') || _.includes(otherTypes, 'http://www.w3.org/2000/01/rdf-schema#Class');
}

export const hasSubClassOf = (resource) => {
  const subclassOf = _.map(resource['http://www.w3.org/2000/01/rdf-schema#subClassOf'], r => r['@id']);
  return !_.isEmpty(subclassOf);
}

export const hasSubPropertyOf = (resource) => {
  const subclassOf = _.map(resource['http://www.w3.org/2000/01/rdf-schema#subClassOf'], r => r['@id']);
  return !_.isEmpty(subclassOf);
}
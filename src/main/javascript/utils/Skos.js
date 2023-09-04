import _ from 'lodash';

import { getType } from './Rdf';

export const getPrefLabel = (resource) => {
  return _.head(_.map(_.filter(resource['http://www.w3.org/2004/02/skos/core#prefLabel'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));
}

export const getAltLabel = (resource) => {
  return _.map(_.filter(resource['http://www.w3.org/2004/02/skos/core#altLabel'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] );
}

export const getNotation = (resource) => {
  return _.map(_.filter(resource['http://www.w3.org/2004/02/skos/core#notation'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] );
}

export const getDefinition = (resource) => {
  return _.head(_.map(_.filter(resource['http://www.w3.org/2004/02/skos/core#definition'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));
}

export const getNote = (resource) => {
  return _.head(_.map(_.filter(resource['http://www.w3.org/2004/02/skos/core#note'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));
}

export const getInScheme = (resource) => {
  return _.head(_.map(resource['http://www.w3.org/2004/02/skos/core#inScheme'], r => r['@id']));
}

export const isConceptScheme = (resource) => {
  return _.includes(getType(resource), 'http://www.w3.org/2004/02/skos/core#ConceptScheme');
}

export const isConcept = (resource) => {
  return _.includes(getType(resource), 'http://www.w3.org/2004/02/skos/core#Concept');  
}
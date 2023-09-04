import _ from 'lodash';

import { getType } from './Rdf';

export const getUsed = (resource) => {
  return _.head(_.map(resource['http://www.w3.org/ns/prov#used'], r => r['@id']));
}

export const getWasAssociatedWith = (resource) => {
  return _.head(_.map(resource['http://www.w3.org/ns/prov#wasAssociatedWith'], r => r['@id']));
}

export const getGenerated = (resource) => {
  return _.map(resource['http://www.w3.org/ns/prov#generated'], r => r['@id']);
}

export const getStartedAtTime = (resource) => {
  return _.head(_.map(resource['http://www.w3.org/ns/prov#startedAtTime'], r => r['@value']));
}

export const isActivity = (resource) => {
  return _.includes(getType(resource), 'http://www.w3.org/ns/prov#Activity');
}

export const isAgent = (resource) => {
  return _.includes(getType(resource), 'http://www.w3.org/ns/prov#Agent');
}
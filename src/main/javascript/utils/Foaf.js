import _ from 'lodash';

export const getName = (resource) => {
  return _.head(_.map(resource['http://xmlns.com/foaf/0.1/name'], r => r['@value']));
}

export const getImg = (resource) => {
  return _.head(_.map(resource['http://xmlns.com/foaf/0.1/img'], r => r['@id']));
}
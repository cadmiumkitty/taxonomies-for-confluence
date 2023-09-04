import _ from 'lodash';

export const getSource = (resource) => {
  return _.head(_.map(resource['http://purl.org/dc/terms/source'], r => r['@id']));
}

export const getSubject = (resource) => {
  return _.head(_.map(resource['http://purl.org/dc/terms/subject'], r => r['@id']));
}

export const getType = (resource) => {
  return _.head(_.map(resource['http://purl.org/dc/terms/type'], r => r['@id']));
}

export const getTitle = (resource) => {
  return _.head(_.map(_.filter(resource['http://purl.org/dc/terms/title'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));
}

export const getRelation = (resource) => {
  return _.head(_.map(resource['http://purl.org/dc/terms/relation'], r => r['@id']));
}

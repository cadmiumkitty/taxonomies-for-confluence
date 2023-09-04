import _ from 'lodash';

export const getTitle = (resource) => {
  return _.head(_.map(_.filter(resource['http://purl.org/dc/elements/1.1/title'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));
}

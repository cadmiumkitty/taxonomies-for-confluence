import _ from 'lodash';

export const getVersion = (resource) => {
  return _.head(_.map(resource['http://purl.org/pav/version'], r => r['@value'] ));
}

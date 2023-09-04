import _ from 'lodash';

import { getLabel as getRdfsLabel } from './Rdfs';
import { getPrefLabel as getSkosPrefLabel } from './Skos';

export const getLabel = (resource) => {
  const label = _.isUndefined(resource)
    ? undefined
    : _.isUndefined(getSkosPrefLabel(resource))
      ? _.isUndefined(getRdfsLabel(resource))
        ? undefined
        : getRdfsLabel(resource)
      : getSkosPrefLabel(resource);
  return label;
}
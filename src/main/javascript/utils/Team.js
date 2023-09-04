import _ from 'lodash';

import { getType } from './Rdf';

export const isPage = (resource) => {
  return _.includes(getType(resource), 'https://dalstonsemantics.com/ns/com/atlassian/page');
}

export const isBlogpost = (resource) => {
  return _.includes(getType(resource), 'https://dalstonsemantics.com/ns/com/atlassian/blogpost');
}

export const isTaxonomyGraph = (resource) => {
  return _.includes(getType(resource), 'https://dalstonsemantics.com/ns/com/atlassian/TaxonomyGraph');
}

export const isBlankDefiningResource = (resource) => {
  return _.includes(getType(resource), 'https://dalstonsemantics.com/ns/com/atlassian/BlankDefiningResource');
}

export const isDefiningResource = (resource) => {
  return _.includes(getType(resource), 'https://dalstonsemantics.com/ns/com/atlassian/DefiningResource');
}

export const getConceptSchemeCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/conceptSchemeCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getConceptCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/conceptCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getClassCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/classCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getPropertyCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/propertyCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getStatementCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/statementCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getSearchScore = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/searchScore'], r => parseFloat(r['@value'])), x => !_.isNaN(x)));
}

export const getStatus = (resource) => {
  return _.head(_.map(_.filter(resource['https://dalstonsemantics.com/ns/com/atlassian/status'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));  
}

export const getStatusTransitionErrorMessage = (resource) => {
  return _.head(_.map(_.filter(resource['https://dalstonsemantics.com/ns/com/atlassian/statusTransitionErrorMessage'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));
}

export const getTaxonomyGraphSequenceNumber = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/taxonomyGraphSequenceNumber'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getInsertedConceptCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/insertedConceptCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getUpdatedConceptCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/updatedConceptCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getDeletedConceptCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/deletedConceptCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getInsertedClassCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/insertedClassCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getUpdatedClassCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/updatedClassCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getDeletedClassCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/deletedClassCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getInsertedPropertyCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/insertedPropertyCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getUpdatedPropertyCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/updatedPropertyCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getDeletedPropertyCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/deletedPropertyCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getImpactedContentCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/impactedContentCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getProcessedContentCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/processedContentCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getFailedContentCount = (resource) => {
  return _.head(_.filter(_.map(resource['https://dalstonsemantics.com/ns/com/atlassian/failedContentCount'], r => parseInt(r['@value'])), x => !_.isNaN(x)));
}

export const getAccountId = (resource) => {
  return _.head(_.map(_.filter(resource['https://dalstonsemantics.com/ns/com/atlassian/accountId'], r => _.isUndefined(r['@language']) || (r['@language'] == 'en')), pl => pl['@value'] ));
}
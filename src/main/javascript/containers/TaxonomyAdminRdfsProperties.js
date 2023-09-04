import _ from 'lodash';

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import EmptyState from '@atlaskit/empty-state';
import Page16Icon from '@atlaskit/icon-object/glyph/page/16';
import Blog16Icon from '@atlaskit/icon-object/glyph/blog/16';
import Lozenge from '@atlaskit/lozenge';
import Spinner from '@atlaskit/spinner';
import TableTree, { Cell, Header, Headers, Row, Rows } from '@atlaskit/table-tree';

import { getSource, getTitle as getDctermsTitle} from '../utils/Dcterms';
import { getTitle as getDcTitle } from '../utils/Dc';
import { getVersion } from '../utils/Pav';
import { getId, isProperty } from '../utils/Rdf';
import { hasSubPropertyOf, getLabel, getIsDefinedBy } from '../utils/Rdfs';
import { isBlogpost, isPage, isBlankDefiningResource, isDefiningResource, getPropertyCount, getStatementCount } from '../utils/Team';

import { getTopProperties, getTopPropertiesForDefiningResource, getSubProperties, getSubPropertiesForDefiningResource, getContentWithPropertyAsPredicate } from '../actions/SchemaProperty';

class TaxonomyAdminRdfsProperties extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
    };
  }

  renderProgress = () => {
    return (
      <div style = {{ marginTop: '1.6em' }}>
        <Spinner />
      </div>
    );
  }

  renderEmpty = () => {
    return (
      <EmptyState 
        imageUrl = 'images/taxonomies-empty.png'
        header = 'No properties to display'
        description = 'Select the snapshot that contains schemas to view properties.'
      />
    )
  }  

  loadChildren = (resource) => {
    const { dispatch, properties, context } = this.props;
    const id = getId(resource);
    if (!properties[id]) {
      if (isBlankDefiningResource(resource)) {
        dispatch(getTopProperties(context));
      } else if (isDefiningResource(resource)) {
        dispatch(getTopPropertiesForDefiningResource(id, context));
      } else if (isProperty(resource)) {
        if (getPropertyCount(resource) > 0) {
          const isDefinedBy = getIsDefinedBy(resource);
          if (_.isEmpty(isDefinedBy)) {
            dispatch(getSubProperties(id, context));
          } else {
            dispatch(getSubPropertiesForDefiningResource(isDefinedBy, id, context));
          }
        } 
        if (getStatementCount(resource) > 0) {
          dispatch(getContentWithPropertyAsPredicate(id));
        }
      }
    }
  }

  renderTypeIcon = (resource) => {
    if (isDefiningResource(resource)) {
      return ( <img src = 'icons/concept-scheme.svg' height = '16px' width = '16px' style = {{float: 'left', paddingTop: '2px'}} /> );
    } else if (isBlankDefiningResource(resource)) {
        return ( <img src = 'icons/concept-scheme.svg' height = '16px' width = '16px' style = {{float: 'left', paddingTop: '2px'}} /> );  
    } else if (isProperty(resource) || hasSubPropertyOf(resource)) {
      return ( <img src = 'icons/property.svg' height = '16px' width = '16px' style = {{float: 'left', paddingTop: '2px'}} /> );
    } else if (isBlogpost(resource)) {
      return ( <Blog16Icon /> );
    } else if (isPage(resource)) {
      return ( <Page16Icon /> );
    } else {
      return ( <Page16Icon /> );
    }
  }

  renderLabel = (resource) => {
    if (isDefiningResource(resource)) {
      const label = getLabel(resource);
      const dctermsTitle = getDctermsTitle(resource);
      const dcTitle = getDcTitle(resource);
      return ( <span style = {{ marginLeft: '8px' }}>{ label ? label : dctermsTitle ? dctermsTitle : dcTitle }</span> );
    } else if (isBlankDefiningResource(resource)) {
      return ( <span style = {{ marginLeft: '8px' }}>{ getLabel(resource) }</span> );
    } else if (isProperty(resource) || hasSubPropertyOf(resource)) {
      return ( <span style = {{ marginLeft: '8px' }}>{ getLabel(resource) }</span> );
    } else {
      const dctermsTitle = getDctermsTitle(resource);
      const dcTitle = getDcTitle(resource);
      return ( <span style = {{ marginLeft: '8px' }}><a href = { getSource(resource) } target = '_blank' style = {{ outline: 'none' }}>{ dctermsTitle ? dctermsTitle : dcTitle }</a></span> );
    }
  }

  renderVersion = (resource) => {
    const version = getVersion(resource);
    if (version) {
      return ( <span style = {{ marginLeft: '8px' }}><Lozenge>{version}</Lozenge></span>)
    }
  }

  renderUri = (resource) => {
    if (isDefiningResource(resource) || isProperty(resource) || hasSubPropertyOf(resource)) {
      return ( <span style = {{ maxWidth: '400px', display: 'inline-block', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>{ getId(resource) }</span> );
    }
  }

  renderPropertyCount = (resource) => {
    if (isDefiningResource(resource) || isBlankDefiningResource(resource) || isProperty(resource) || hasSubPropertyOf(resource)) {
      return ( <span>{ getPropertyCount(resource) }</span> );
    }
  }

  renderTree = () => {
    const { definingResources, properties } = this.props;
    return (
      <div style = {{ marginTop: '1.6em' }}>
        <TableTree>
          <Headers>
            <Header width = { 900 }>Label</Header>
            <Header width = { 100 }>Properties</Header>
            <Header>URI</Header>
          </Headers>
          <Rows
            items= { definingResources }
            render={ 
              (item) => {
                const propertyCount = getPropertyCount(item);
                const statementCount = getStatementCount(item);
                return (
                  <Row
                    expandLabel = 'Expand'
                    collapseLabel = 'Collapse'
                    itemId = { getId(item) }
                    onExpand = { this.loadChildren.bind(this, item) }
                    items = { properties[getId(item)] }
                    hasChildren = { propertyCount > 1 || statementCount > 0 }
                  >
                    <Cell singleLine>{ this.renderTypeIcon(item) }{ this.renderLabel(item) }{ this.renderVersion(item) }</Cell>
                    <Cell singleLine>{ this.renderPropertyCount(item) }</Cell>
                    <Cell singleLine>{ this.renderUri(item) }</Cell>
                  </Row>
                );
              }
            }
          />
        </TableTree>
      </div>
    );
  }

  render() {
    const { emptyDefiningResourceForPropertiesRequestStarted, definingResourceForPropertiesRequestStarted, definingResources } = this.props;
    return (
      <div style = {{ overflow: 'hidden', width: '100%', height: '100%' }}>
        { (emptyDefiningResourceForPropertiesRequestStarted || definingResourceForPropertiesRequestStarted)
            ? this.renderProgress()
            : _.size(definingResources) > 0
                ? this.renderTree() 
                : this.renderEmpty() }
      </div>
    );
  }
}

TaxonomyAdminRdfsProperties.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { emptyDefiningResourceForPropertiesRequestStarted, definingResourceForPropertiesRequestStarted, definingResources, properties, context} = state.schemaProperty;
  return {
    emptyDefiningResourceForPropertiesRequestStarted,
    definingResourceForPropertiesRequestStarted,
    definingResources,
    properties,
    context
  };
}

export default connect(mapStateToProps)(TaxonomyAdminRdfsProperties);
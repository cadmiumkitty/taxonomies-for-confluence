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
import { getId } from '../utils/Rdf';
import { isClass, hasSubClassOf, getLabel, getIsDefinedBy } from '../utils/Rdfs';
import { isBlogpost, isPage, isBlankDefiningResource, isDefiningResource, getClassCount, getStatementCount } from '../utils/Team';

import { getTopClasses, getTopClassesForDefiningResource, getSubClasses, getSubClassesForDefiningResource, getContentWithClassAsObject } from '../actions/SchemaClass';

class TaxonomyAdminRdfsClasses extends React.Component {

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
        header = 'No classes to display'
        description = 'Select the snapshot that contains schemas to view classes.'
      />
    )
  }  

  loadChildren = (resource) => {
    const { dispatch, classes, context } = this.props;
    const id = getId(resource);
    if (!classes[id]) {
      if (isBlankDefiningResource(resource)) {
        dispatch(getTopClasses(context));
      } else if (isDefiningResource(resource)) {
        dispatch(getTopClassesForDefiningResource(id, context));
      } else if (isClass(resource)) {
        if (getClassCount(resource) > 0) {
          const isDefinedBy = getIsDefinedBy(resource);
          if (_.isEmpty(isDefinedBy)) {
            dispatch(getSubClasses(id, context));
          } else {
            dispatch(getSubClassesForDefiningResource(isDefinedBy, id, context));
          }
        } 
        if (getStatementCount(resource) > 0) {
          dispatch(getContentWithClassAsObject(id));
        }
      }
    }
  }

  renderTypeIcon = (resource) => {
    if (isDefiningResource(resource)) {
      return ( <img src = 'icons/concept-scheme.svg' height = '16px' width = '16px' style = {{float: 'left', paddingTop: '2px'}} /> );
    } else if (isBlankDefiningResource(resource)) {
        return ( <img src = 'icons/concept-scheme.svg' height = '16px' width = '16px' style = {{float: 'left', paddingTop: '2px'}} /> );  
    } else if (isClass(resource) || hasSubClassOf(resource)) {
      return ( <img src = 'icons/class.svg' height = '16px' width = '16px' style = {{float: 'left', paddingTop: '2px'}} /> );
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
    } else if (isClass(resource) || hasSubClassOf(resource)) {
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
    if (isDefiningResource(resource) || isClass(resource) || hasSubClassOf(resource)) {
      return ( <span style = {{ maxWidth: '400px', display: 'inline-block', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>{ getId(resource) }</span> );
    }
  }

  renderClassCount = (resource) => {
    if (isDefiningResource(resource) || isBlankDefiningResource(resource) || isClass(resource) || hasSubClassOf(resource)) {
      return ( <span>{ getClassCount(resource) }</span> );
    }
  }

  renderTree = () => {
    const { definingResources, classes } = this.props;
    return (
      <div style = {{ marginTop: '1.6em' }}>
        <TableTree>
          <Headers>
            <Header width = { 900 }>Label</Header>
            <Header width = { 100 }>Classes</Header>
            <Header>URI</Header>
          </Headers>
          <Rows
            items= { definingResources }
            render={ 
              (item) => {
                const classCount = getClassCount(item);
                const statementCount = getStatementCount(item);
                return (
                  <Row
                    expandLabel = 'Expand'
                    collapseLabel = 'Collapse'
                    itemId = { getId(item) }
                    onExpand = { this.loadChildren.bind(this, item) }
                    items = { classes[getId(item)] }
                    hasChildren = { ((isDefiningResource(item) || isBlankDefiningResource(item)) && classCount > 0) || (classCount > 1) || (statementCount > 0) }
                  >
                    <Cell singleLine>{ this.renderTypeIcon(item) }{ this.renderLabel(item) }{ this.renderVersion(item) }</Cell>
                    <Cell singleLine>{ this.renderClassCount(item) }</Cell>
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
    const { emptyDefiningResourceForClassesRequestStarted, definingResourceForClassesRequestStarted, definingResources } = this.props;
    return (
      <div style = {{ overflow: 'hidden', width: '100%', height: '100%' }}>
        { (emptyDefiningResourceForClassesRequestStarted || definingResourceForClassesRequestStarted)
            ? this.renderProgress()
            : _.size(definingResources) > 0
                ? this.renderTree() 
                : this.renderEmpty() }
      </div>
    );
  }
}

TaxonomyAdminRdfsClasses.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { emptyDefiningResourceForClassesRequestStarted, definingResourceForClassesRequestStarted, definingResources, classes, context} = state.schemaClass;
  return {
    emptyDefiningResourceForClassesRequestStarted,
    definingResourceForClassesRequestStarted,
    definingResources,
    classes,
    context
  };
}

export default connect(mapStateToProps)(TaxonomyAdminRdfsClasses);
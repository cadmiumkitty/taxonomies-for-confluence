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

import { getTopConcepts, getNarrowerConcepts, getContent } from '../actions/Taxonomy';

import { getSource, getTitle as getDctermsTitle } from '../utils/Dcterms';
import { getTitle as getDcTitle} from '../utils/Dc';
import { getVersion } from '../utils/Pav';
import { getId } from '../utils/Rdf';
import { getLabel } from '../utils/Rdfs';
import { isConceptScheme, isConcept, getPrefLabel, getNotation } from '../utils/Skos';
import { isBlogpost, isPage, getConceptCount, getStatementCount } from '../utils/Team';

class TaxonomyAdminSkosTaxonomies extends React.Component {

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
        header = 'No concept schemes to display'
        description = 'Select the snapshot that contains concept schemes to view concepts.'
      />
    )
  }

  loadChildren = (resource) => {
    const { dispatch, concepts, context } = this.props;
    const id = getId(resource);
    if (!concepts[id]) {
      if (isConceptScheme(resource)) {
        dispatch(getTopConcepts(id, context));
      } else if (isConcept(resource)) {
        if (getConceptCount(resource) > 0) {
          dispatch(getNarrowerConcepts(id, context));
        } 
        if (getStatementCount(resource) > 0) {
          dispatch(getContent(id));
        }
      }
    }
  }

  renderTypeIcon = (resource) => {
    if (isConceptScheme(resource)) {
      return ( <img src = 'icons/concept-scheme.svg' height = '16px' width = '16px' style = {{float: 'left', paddingTop: '2px'}} /> );
    } else if (isConcept(resource)) {
      return ( <img src = 'icons/concept.svg' height = '16px' width = '16px' style = {{float: 'left', paddingTop: '2px'}} /> );
    } else if (isBlogpost(resource)) {
      return ( <Blog16Icon /> );
    } else if (isPage(resource)) {
      return ( <Page16Icon /> );
    } else {
      return ( <Page16Icon /> );
    }
  }

  renderLabel = (resource) => {
    if (isConceptScheme(resource)) {
      const prefLabel = getPrefLabel(resource);
      const label = getLabel(resource);
      const dctermsTitle = getDctermsTitle(resource);
      const dcTitle = getDcTitle(resource);
      return ( <span style = {{ marginLeft: '8px' }}>{ prefLabel ? prefLabel : label ? label : dctermsTitle ? dctermsTitle : dcTitle }</span> );
    } else if (isConcept(resource)) {
      const prefLabel = getPrefLabel(resource);
      return ( <span style = {{ marginLeft: '8px' }}>{ prefLabel }</span> );
    } else {
      const dctermsTitle = getDctermsTitle(resource);
      return ( <span style = {{ marginLeft: '8px' }}><a href = { getSource(resource) } target = '_blank' style = {{ outline: 'none' }}>{ dctermsTitle }</a></span> );
    }
  }

  renderVersion = (resource) => {
    const version = getVersion(resource);
    if (version) {
      return ( <span style = {{ marginLeft: '8px' }}><Lozenge>{version}</Lozenge></span>)
    }
  }

  renderUri = (resource) => {
    if (isConceptScheme(resource) || isConcept(resource)) {
      return ( <span style = {{ maxWidth: '400px', display: 'inline-block', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>{ getId(resource) }</span> );
    }
  }

  renderConceptCount = (resource) => {
    if (isConceptScheme(resource) || isConcept(resource)) {
      return ( <span>{ getConceptCount(resource) }</span> );
    }
  }

  renderTree = () => {
    const { conceptSchemes, concepts } = this.props;
    return (
      <div style = {{ marginTop: '1.6em' }}>
        <TableTree>
          <Headers>
            <Header width = { 750 }>Preferred Label</Header>
            <Header width = { 150 }>Notation</Header>
            <Header width = { 100 }>Concepts</Header>
            <Header>URI</Header>
          </Headers>
          <Rows
            items= { conceptSchemes }
            render={ 
              (item) => {
                const conceptCount = getConceptCount(item);
                const statementCount = getStatementCount(item);
                return (
                  <Row
                    expandLabel = 'Expand'
                    collapseLabel = 'Collapse'
                    itemId = { getId(item) }
                    onExpand = { this.loadChildren.bind(this, item) }
                    items = { concepts[getId(item)] }
                    hasChildren = { conceptCount > 1 || statementCount > 0 }
                  >
                    <Cell singleLine>{ this.renderTypeIcon(item) }{ this.renderLabel(item) }{ this.renderVersion(item) }</Cell>
                    <Cell singleLine>{ _.head(getNotation(item)) }</Cell>
                    <Cell singleLine>{ this.renderConceptCount(item) }</Cell>
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
    const { conceptSchemesRequestStarted, conceptSchemes } = this.props;
    return (
      <div style = {{ overflow: 'hidden', width: '100%', height: '100%' }}>
        { conceptSchemesRequestStarted 
            ? this.renderProgress()
            : _.size(conceptSchemes) > 0
                ? this.renderTree() 
                : this.renderEmpty() }
      </div>
    );
  }
}

TaxonomyAdminSkosTaxonomies.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { uploadTaxonomyRequestSarted, conceptSchemesRequestStarted, conceptSchemes, concepts, context} = state.taxonomy;
  return {
    uploadTaxonomyRequestSarted,
    conceptSchemesRequestStarted,
    conceptSchemes,
    concepts,
    context
  };
}

export default connect(mapStateToProps)(TaxonomyAdminSkosTaxonomies);
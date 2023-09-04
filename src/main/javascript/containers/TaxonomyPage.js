import _ from 'lodash';

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import Avatar from '@atlaskit/avatar';
import Button from '@atlaskit/button';
import EmptyState from '@atlaskit/empty-state';
import Page16Icon from '@atlaskit/icon-object/glyph/page/16';
import Blog16Icon from '@atlaskit/icon-object/glyph/blog/16';
import Lozenge from '@atlaskit/lozenge';
import Page from '@atlaskit/page';
import PageHeader from '@atlaskit/page-header';
import { RightSidePanel, FlexContainer } from '@atlaskit/right-side-panel';
import Spinner from '@atlaskit/spinner';
import TableTree, { Cell, Header, Headers, Row, Rows } from '@atlaskit/table-tree';

import { getConceptSchemes, getTopConcepts, getNarrowerConcepts, getContent, getResourceProvenance } from '../actions/Taxonomy';

import { getRelation, getSource, getSubject, getType, getTitle } from '../utils/Dcterms';
import { getName, getImg } from '../utils/Foaf';
import { getVersion } from '../utils/Pav';
import { isAgent, isActivity, getWasAssociatedWith, getUsed, getStartedAtTime } from '../utils/Prov';
import { getId } from '../utils/Rdf';
import { getLabel } from '../utils/Rdfs';
import { isConceptScheme, isConcept, getPrefLabel, getAltLabel, getNotation, getDefinition, getNote} from '../utils/Skos';
import { isBlogpost, isPage, getConceptCount, getStatementCount } from '../utils/Team';

class TaxonomyPage extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
      timezone: undefined
    };
  }

  componentDidMount = () => {
    const { dispatch } = this.props;
    dispatch(getConceptSchemes());
    AP.user.getTimeZone(timezone => {
      this.setState({
        ...this.state,
        timezone
      });
    });
  }

  renderConceptSchemesProgress = () => {
    return (
      <Spinner />
    );
  }

  renderConceptSchemesEmpty = () => {
    return (
      <EmptyState 
        imageUrl = 'images/taxonomies-empty.png'
        header = 'No taxonomies uploaded'
        description = 'When the site administrator uploads taxonomies, start tracking completeness of architecture, governance, or regulatory information on Confluence.'
      />
    )
  }

  loadChildren = (resource) => {
    const { dispatch, concepts } = this.props;
    const id = getId(resource);
    if (!concepts[id]) {
      if (isConceptScheme(resource)) {
        dispatch(getTopConcepts(id));
      } else if (isConcept(resource)) {
        if (getConceptCount(resource) > 0) {
          dispatch(getNarrowerConcepts(id));
        } 
        if (getStatementCount(resource) > 0) {
          dispatch(getContent(id));
        }
      }
    }
  }

  loadDetails = (resource, e) => {
    const { dispatch } = this.props;
    const label = e.target.getAttribute('aria-label');
    if (label != 'Expand' && label != 'Collapse') {
      this.setState({isOpen: true});
      dispatch(getResourceProvenance(resource));
    }
  }

  closeDetails = () => {
    this.setState({isOpen: false});
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
      return ( <span style = {{ marginLeft: '8px' }}>{ prefLabel ? prefLabel : getLabel(resource) }</span> );
    } else if (isConcept(resource)) {
      return ( <span style = {{ marginLeft: '8px' }}>{ getPrefLabel(resource) }</span> );
    } else {
      return ( <span style = {{ marginLeft: '8px' }}><a href = { getSource(resource) } target = '_blank' style = {{ outline: 'none' }}>{ getTitle(resource) }</a></span> );
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
      return ( <span>{ getId(resource) }</span> );
    }
  }

  renderConceptCount = (resource) => {
    if (isConceptScheme(resource) || isConcept(resource)) {
      return ( <span>{ getConceptCount(resource) }</span> );
    }
  }

  renderStatementCount = (resource) => {
    if (isConceptScheme(resource) || isConcept(resource)) {
      return ( <span>{ getStatementCount(resource) }</span> );
    }
  }

  renderConceptSchemes = () => {
    const { conceptSchemes, concepts } = this.props;
    return (
      <TableTree>
        <Headers>
          <Header width = { 650 }>Preferred Label</Header>
          <Header width = { 150 }>Notation</Header>
          <Header width = { 100 }>Concepts</Header>
          <Header width = { 100 }>Statements</Header>
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
                  <Cell onClick = { this.loadDetails.bind(this, item) } singleLine>{ this.renderTypeIcon(item) }{ this.renderLabel(item) }{ this.renderVersion(item) }</Cell>
                  <Cell onClick = { this.loadDetails.bind(this, item) } singleLine>{ _.head(getNotation(item)) }</Cell>
                  <Cell onClick = { this.loadDetails.bind(this, item) } singleLine>{ this.renderConceptCount(item) }</Cell>
                  <Cell onClick = { this.loadDetails.bind(this, item) } singleLine>{ this.renderStatementCount(item) }</Cell>
                </Row>
              );
            }
          }
        />
      </TableTree>
    );
  }

  renderResourceProvenanceProgress = () => {
    return (
      <Spinner />
    );
  }

  renderResourceProvenance = () => {
    const { resource, provenance } = this.props;
    const { timezone } = this.state;
    const activity = _.filter(provenance, p => isActivity(p));

    const dateTimeFormatLanguage = 'en';
    const dateTimeFormatOptions = { year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: 'numeric', hour12: true, timeZone: timezone, timeZoneName: 'short' };
    const dateTimeFormat = new Intl.DateTimeFormat(dateTimeFormatLanguage, dateTimeFormatOptions);

    return (_.map(_.orderBy(activity, a => { return getStartedAtTime(a) }, 'desc'), a => {
      const agent = _.find(provenance, p => isAgent(p) && (getId(p) == getWasAssociatedWith(a)));
      const used = _.find(provenance, p => getId(p) == getUsed(a));
      const subject = _.find(provenance, p => isConcept(p) && (getId(p) == getSubject(used)));
      const type = _.find(provenance, p => isConcept(p) && (getId(p) == getType(used)));
      const relation = _.find(provenance, p => isConcept(p) && (getId(p) == getRelation(used)));

      const startedAtTime = new Date(getStartedAtTime(a));
      return (
        <div style = {{ display: 'flex', alignItems: 'center', marginTop: '12px' }} key = { getId(a) }>
          <Avatar appearance = 'circle' src = { getImg(agent) } />
          <span style = {{ marginLeft: '8px' }}>
            { getName(agent) } { (isConceptScheme(resource) || isConcept(resource)) 
                ? 'imported' 
                : _.isUndefined(relation)
                    ? _.isUndefined(subject) 
                        ? _.isUndefined(type)
                          ? 'done something else'
                          : `set type to ${getPrefLabel(type)}` 
                        : `set subject to ${getPrefLabel(subject)}` 
                    : `set relation to ${getPrefLabel(relation)}` } on { dateTimeFormat.format(startedAtTime) }
          </span>
        </div>
      );
    }));
  }

  renderResource = () => {
    const {resource, provenance, provenanceRequestStarted } = this.props;
    const prefLabel = getPrefLabel(resource);
    const label = getLabel(resource);
    const title = getTitle(resource);
    const altLabel = _.join(getAltLabel(resource));
    const definition = getDefinition(resource);
    const note = getNote(resource);
    return (
      <div style = {{ paddingLeft: '18px' }}>
        <div style = {{ textAlign: 'right', marginTop: '4px' }}>
          <Button onClick = { this.closeDetails.bind(this) }>Close</Button>
        </div>
        <PageHeader>{ prefLabel ? prefLabel : label ? label : title }</PageHeader>
        <Page>
          {_.size(altLabel) > 0 ? <div><h4>Alternative Labels</h4><p>{ altLabel }</p></div> : <div/> }
          {_.size(definition) > 0 ? <div style = {{ marginTop: '8px' }}><h4>Definition</h4><p>{ definition }</p></div> : <div/> }
          {_.size(note) > 0 ? <div style = {{ marginTop: '8px' }}><h4>Note</h4><p>{ note }</p></div> : <div/> }
          <h4>Activity</h4>
          { provenanceRequestStarted 
              ? this.renderResourceProvenanceProgress() 
              : _.size(provenance) > 0
                  ? this.renderResourceProvenance()
                  : <div>No provenance information available.</div> }
        </Page>
      </div>
    );
  }

  render() {
    const { conceptSchemesRequestStarted, conceptSchemes, resource} = this.props;
    const { isOpen } = this.state;
    return (
      <div style = {{ overflow: 'hidden', paddingLeft: '18px', paddingRight: '18px' }} id = "ac-iframe-options" data-options = "resize:false;sizeToParent:true">
        <PageHeader>Taxonomies</PageHeader>
        <FlexContainer id = 'tableTreeContainer'>
          <Page>
            { conceptSchemesRequestStarted 
                ? this.renderConceptSchemesProgress()
                : _.size(conceptSchemes) > 0
                    ? this.renderConceptSchemes() 
                    : this.renderConceptSchemesEmpty() }
          </Page>
          <RightSidePanel isOpen={isOpen} attachPanelTo='tableTreeContainer'>
            { _.isUndefined(resource)
                ? <div/>
                : this.renderResource() }
          </RightSidePanel>
        </FlexContainer>
      </div>
    );
  }
}

TaxonomyPage.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { conceptSchemesRequestStarted, concepts, conceptSchemes, provenanceRequestStarted, resource, provenance } = state.taxonomy;
  return {
    conceptSchemesRequestStarted,
    concepts,
    conceptSchemes,
    provenanceRequestStarted,
    resource,
    provenance
  };
}

export default connect(mapStateToProps)(TaxonomyPage);
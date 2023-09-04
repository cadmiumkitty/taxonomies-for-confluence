import _ from 'lodash';

import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';

import Avatar from '@atlaskit/avatar';
import Button, { ButtonGroup, LoadingButton } from '@atlaskit/button';
import { Checkbox } from "@atlaskit/checkbox";
import DropdownMenu, { DropdownItem, DropdownItemGroup } from '@atlaskit/dropdown-menu';
import DynamicTable from '@atlaskit/dynamic-table';

import MoreIcon from '@atlaskit/icon/glyph/more';
import ShortcutIcon from '@atlaskit/icon/glyph/shortcut'

import InlineMessage from '@atlaskit/inline-message';
import Lozenge from '@atlaskit/lozenge';
import Modal, { ModalBody, ModalFooter, ModalHeader, ModalTitle, ModalTransition } from '@atlaskit/modal-dialog';
import Spinner from '@atlaskit/spinner';

import { getConceptSchemes } from '../actions/Taxonomy';
import { getTaxonomyVersions, calculateContentImpact, transitionToCurrent, cancelTransitionToCurrent, clear, importTaxonomyFile, importTaxonomyCatalog, copyFromCurrent } from '../actions/TaxonomyVersions';

import { getImg } from '../utils/Foaf';
import { isActivity, isAgent, getGenerated, getWasAssociatedWith, getStartedAtTime } from '../utils/Prov';
import { getId } from '../utils/Rdf';
import { isTaxonomyGraph, getStatus, 
  getStatusTransitionErrorMessage,
  getTaxonomyGraphSequenceNumber, 
  getConceptCount, getClassCount, getPropertyCount,
  getInsertedConceptCount, getUpdatedConceptCount, getDeletedConceptCount, 
  getInsertedClassCount, getUpdatedClassCount, getDeletedClassCount, 
  getInsertedPropertyCount, getUpdatedPropertyCount, getDeletedPropertyCount, 
  getImpactedContentCount, 
  getProcessedContentCount, getFailedContentCount,
  getAccountId} from '../utils/Team';
import { getDefiningResourcesForClasses } from '../actions/SchemaClass';
import { getDefiningResourcesForProperties } from '../actions/SchemaProperty';

class TaxonomyVersions extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      highlightedRowContext: undefined,
      dropdownOpen: false,
      importTaxonomyFileOpen: false,
      file: undefined,
      importTaxonomyCatalogOpen: false,
      scheme: [],
      copyFromCurrentOpen: false,
      clearOpen: false,
      calculateContentImpactOpen: false,
      switchOpen: true,
      timezone: undefined
    };    
  }
  
  componentDidMount = () => {
    AP.user.getTimeZone(timezone => {
      this.setState({
        ...this.state,
        timezone
      });
    });
  }

  componentDidUpdate = () => {
    const { versions } = this.props;
    if (_.find(versions, version => {
        const status = getStatus(version);
        const isTG = isTaxonomyGraph(version);
        const isPollingStatus = _.includes(['importing', 'copying', 'clearing', 'calculating_taxonomy_version_difference', 'calculating_content_impact', 'transitioning_to_current', 'cancelling_transition_to_current'], status);
        return isTG && isPollingStatus;
      })) {
      if (!this.interval) {
        this.interval = setInterval(this.onInterval, 10000);
      }
    } else if (this.interval) {
      clearInterval(this.interval);
      this.interval = undefined;
    }
  }

  componentWillUnmount = () => {
    clearInterval(this.interval);
    this.interval = undefined;
  }

  onInterval = () => {
    const { dispatch } = this.props;
    dispatch(getTaxonomyVersions(false));
  }

  renderStatus = (status) => {
    switch (status) {
      case 'calculating_taxonomy_version_difference':
        return <Lozenge>checking difference</Lozenge>;
      case 'calculating_content_impact':
        return <Lozenge>checking impact</Lozenge>;
      case 'awaiting_transition_to_current':
        return <Lozenge>awaiting switch</Lozenge>;
      case 'transitioning_to_current':
        return <Lozenge>switching</Lozenge>;
      case 'cancelling_transition_to_current':
        return <Lozenge>cancelling switch</Lozenge>;
      default:
        return <Lozenge>{ status }</Lozenge>;
      }
  }

  onRowClick = (context, e) => {
    const { dispatch } = this.props;
    const ariaLabel = e.target.getAttribute('aria-label');
    const label = e.target.getAttribute('label');
    if (ariaLabel != 'Actions' && label != 'Actions') {
      this.setState({
        ...this.state,
        highlightedRowContext: context
      })
      dispatch(getConceptSchemes(context));
      dispatch(getDefiningResourcesForClasses(context));
      dispatch(getDefiningResourcesForProperties(context));
    }
  }

  onClickCompleteSwitch = (e) => {
    const { dispatch } = this.props;
    e.stopPropagation();
    dispatch(transitionToCurrent());
    this.setState({
      ...this.state,
      switchOpen: false
    })
  }

  onClickCancelSwitch = (e) => {
    const { dispatch } = this.props;
    e.stopPropagation();
    dispatch(cancelTransitionToCurrent());
    this.setState({
      ...this.state,
      switchOpen: false
    })
  }
  
  onDropdownOpenChange = (dropdownState) => {
    this.setState({
      ...this.state,
      dropdownOpen: dropdownState.isOpen
    })
  }

  onClickImportTaxonomyCatalogDropdownItem = (e) => {
    e.stopPropagation();
    this.setState({ 
      ... this.state,
      dropdownOpen: false,
      importTaxonomyCatalogOpen: true,
      scheme: []
    });
  }

  onClickImportTaxonomyFileDropdownItem = (e) => {
    e.stopPropagation();
    this.setState({ 
      ... this.state,
      dropdownOpen: false,
      importTaxonomyFileOpen: true,
      file: undefined
    });
  }

  onClickCopyFromCurrentDropdownItem = (e) => {
    e.stopPropagation();
    this.setState({ 
      ... this.state,
      dropdownOpen: false,
      copyFromCurrentOpen: true
    });
  }

  onClickClearDropdownItem = (e) => {
    e.stopPropagation();
    this.setState({ 
      ... this.state,
      dropdownOpen: false,
      clearOpen: true
    });
  }
  
  onClickCalculateContentImpactDropdownItem = (e) => {
    e.stopPropagation();
    this.setState({ 
      ... this.state,
      dropdownOpen: false,
      calculateContentImpactOpen: true,
      switchOpen: true
    });
  }

  createHead = () => {

    return {
      cells: [
        {
          key: 'snapshot',
          content: <b>Snapshot</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'status',
          content: <b>Status</b>,
          isSortable: false,
          width: 35
        },
        {
          key: 'created',
          content: <b>Created</b>,
          isSortable: false,
          width: 10
        },
        {
          key: 'concepts',
          content: <b>Concepts</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'classes',
          content: <b>Classes</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'properties',
          content: <b>Properties</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'inserted',
          content: <b>Added</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'updated',
          content: <b>Updated</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'deleted',
          content: <b>Removed</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'impacted',
          content: <b>Impacted</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'processed',
          content: <b>Processed</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'failed',
          content: <b>Failed</b>,
          isSortable: false,
          width: 5
        },
        {
          key: 'actions',
          content: <b>Actions</b>,
          isSortable: false,
          width: 5
        }
      ]
    };
  }

  createRows = () => {

    const { cancelTransitionToCurrentRequestStarted, transitionToCurrentRequestStarted, versions } = this.props;
    const { dropdownOpen, timezone } = this.state;

    const dateTimeFormatLanguage = 'en';
    const dateTimeFormatOptions = { year: 'numeric', month: 'short', day: 'numeric', timeZone: timezone};
    const dateTimeFormat = new Intl.DateTimeFormat(dateTimeFormatLanguage, dateTimeFormatOptions);

    return _.map(_.reverse(_.sortBy(_.filter(versions, isTaxonomyGraph), getTaxonomyGraphSequenceNumber)), (version) => {

      const id = getId(version);
      const taxonomyGraphSequenceNumber = getTaxonomyGraphSequenceNumber(version);
      const status = getStatus(version);
      const statusTransitionErrorMessage = getStatusTransitionErrorMessage(version);
      const conceptCount = getConceptCount(version);
      const classCount = getClassCount(version);
      const propertyCount = getPropertyCount(version);
      const insertedConceptCount = getInsertedConceptCount(version);
      const updatedConceptCount = getUpdatedConceptCount(version);
      const deletedConceptCount = getDeletedConceptCount(version);
      const insertedClassCount = getInsertedClassCount(version);
      const updatedClassCount = getUpdatedClassCount(version);
      const deletedClassCount = getDeletedClassCount(version);
      const insertedPropertyCount = getInsertedPropertyCount(version);
      const updatedPropertyCount = getUpdatedPropertyCount(version);
      const deletedPropertyCount = getDeletedPropertyCount(version);
      const impactedContentCount = getImpactedContentCount(version);
      const processedContentCount = getProcessedContentCount(version);
      const failedContentCount = getFailedContentCount(version);

      const onClick = this.onRowClick.bind(this, id);
      
      const activity = _.find(versions, (v) => {
        const isA = isActivity(v);
        const generated = getGenerated(v);
        const foundGenerated = _.includes(generated, getId(version));
        return isA && foundGenerated;
      });
      const agent = _.find(versions, (v) => {
        const isA = isAgent(v);
        const wasAssociatedWith = getWasAssociatedWith(activity);
        const found = getId(v) == wasAssociatedWith;
        return isA & found;
      });
      const accountId = getAccountId(agent);

      const showConceptSchemeAndConceptCounts = _.includes(['draft', 'calculating_taxonomy_version_difference', 'awaiting_transition_to_current', 'transitioning_to_current', 'current', 'historical'], status);
      const showInsertedUpdatedDeletedCounts = _.includes(['draft', 'awaiting_transition_to_current', 'transitioning_to_current', 'current', 'historical'], status);
      const showContentCounts = _.includes(['awaiting_transition_to_current', 'transitioning_to_current', 'current', 'historical'], status);

      return {
        key: id,
        cells: [
          {
            key: `version-${id}`,
            content: <span style = {{ marginLeft: '6px' }}>{ taxonomyGraphSequenceNumber }</span>
          },
          {
            key: `status-${id}`,
            content: <div style = {{ display: 'flex', alignItems: 'center' }}>
              { status != 'historical' ? this.renderStatus(status) : <div/> }
              <div style = {{ marginLeft: '16px' }}>
                { _.includes(['draft'], status) && statusTransitionErrorMessage ? <InlineMessage type = 'error' title = 'Error processing the request'>{ statusTransitionErrorMessage }</InlineMessage> : <div/> }
                { _.includes(['importing', 'copying', 'clearing', 'calculating_taxonomy_version_difference', 'calculating_content_impact', 'transitioning_to_current', 'cancelling_transition_to_current'], status) ? <Spinner/> : <div/> }
                { _.includes(['awaiting_transition_to_current'], status) && (this.state.switchOpen || cancelTransitionToCurrentRequestStarted || transitionToCurrentRequestStarted)
                  ? <ButtonGroup>
                      <LoadingButton isDisabled = { cancelTransitionToCurrentRequestStarted } isLoading = { transitionToCurrentRequestStarted } onClick = { this.onClickCompleteSwitch } appearance='warning' spacing = 'compact'>Complete switch</LoadingButton>
                      <LoadingButton isDisabled = { transitionToCurrentRequestStarted } isLoading = { cancelTransitionToCurrentRequestStarted } onClick = { this.onClickCancelSwitch } appearance='subtle' spacing = 'compact'>Cancel switch</LoadingButton>
                    </ButtonGroup>
                  : <div/> 
                }
              </div>
            </div>
          },
          {
            key: `created-${id}`,
            content: accountId ? <div style = {{display: 'flex', alignItems: 'center'}}><Avatar size = 'small' appearance = 'circle' src = { getImg(agent) } /><span style = {{ marginLeft: '4px' }}>{ dateTimeFormat.format(new Date(getStartedAtTime(activity))) }</span></div> : <div/>
          },
          {
            key: `concepts-${id}`,
            content: showConceptSchemeAndConceptCounts ? <span style = {{ marginLeft: '6px' }}>{ conceptCount }</span> : <div/>
          },
          {
            key: `classes-${id}`,
            content: showConceptSchemeAndConceptCounts ? <span style = {{ marginLeft: '6px' }}>{ classCount }</span> : <div/>
          },
          {
            key: `properties-${id}`,
            content: showConceptSchemeAndConceptCounts ? <span style = {{ marginLeft: '6px' }}>{ propertyCount }</span> : <div/>
          },
          {
            key: `inserted-${id}`,
            content: showInsertedUpdatedDeletedCounts ? <span style = {{ marginLeft: '6px' }}>{ insertedConceptCount + insertedClassCount + insertedPropertyCount }</span> : <div/>
          },
          {
            key: `updated-${id}`,
            content: showInsertedUpdatedDeletedCounts ? <span style = {{ marginLeft: '6px' }}>{ updatedConceptCount + updatedClassCount + updatedPropertyCount }</span> : <div/>
          },
          {
            key: `deleted-${id}`,
            content: showInsertedUpdatedDeletedCounts ? <span style = {{ marginLeft: '6px' }}>{ deletedConceptCount + deletedClassCount + deletedPropertyCount }</span> : <div/>
          },
          {
            key: `impacted-${id}`,
            content: showContentCounts ? <span style = {{ marginLeft: '6px' }}>{impactedContentCount}</span> : <div/>
          },
          {
            key: `processed-${id}`,
            content: showContentCounts ? <span style = {{ marginLeft: '6px' }}>{ processedContentCount }</span> : <div/>
          },
          {
            key: `failed-${id}`,
            content: showContentCounts ? <span style = {{ marginLeft: '6px' }}>{ failedContentCount }</span> : <div/>
          },
          {
            key: `actions-${id}`,
            content: status != 'current' && status != 'historical' 
                ? <DropdownMenu isOpen = { dropdownOpen } onOpenChange = { this.onDropdownOpenChange.bind(this) } trigger= { ({ triggerRef, ...props }) => (<Button {...props} appearance = 'subtle' spacing = 'compact' label = 'Actions' iconBefore={ <MoreIcon label = 'Actions' /> } ref = { triggerRef } />) }>
                    <DropdownItemGroup>
                      <DropdownItem isDisabled = { status != 'draft' } onClick = { this.onClickImportTaxonomyCatalogDropdownItem.bind(this) }>Import taxonomies and schemas from catalog</DropdownItem>
                      <DropdownItem isDisabled = { status != 'draft' } onClick = { this.onClickImportTaxonomyFileDropdownItem.bind(this) }>Import taxonomies and schemas from file</DropdownItem>
                      <DropdownItem isDisabled = { status != 'draft' } onClick = { this.onClickCopyFromCurrentDropdownItem.bind(this) }>Copy taxonomies and schemas from current snapshot</DropdownItem>
                      <DropdownItem isDisabled = { status != 'draft' } onClick = { this.onClickClearDropdownItem.bind(this) }>Clear draft snapshot</DropdownItem>
                      <DropdownItem isDisabled = { status != 'draft' } onClick = { this.onClickCalculateContentImpactDropdownItem.bind(this) }>Start switching to draft snapshot</DropdownItem>
                    </DropdownItemGroup>
                  </DropdownMenu>
                : <div/>
          }
        ],
        onClick
      }
    });
  }

  onClickCloseImportTaxonomyFileModal = (e) => {
    this.setState({
      ...this.state,
      importTaxonomyFileOpen: false,
    })
  }

  onTaxonomyFileChange = (e) => {
    const file = e.target.files;
    this.setState({ 
      ...this.state,
      file: file
    });
  }

  onClickImportTaxonomyFileModal = (e) => {
    const { dispatch } = this.props;
    const { file } = this.state;
    if (!_.isEmpty(file)) {
      dispatch(importTaxonomyFile(_.head(file)));
      this.setState({
        ...this.state,
        importTaxonomyFileOpen: false
      })
    }
  }

  onClickCloseImportTaxonomyCatalogModal = (e) => {
    this.setState({
      ...this.state,
      importTaxonomyCatalogOpen: false,
    })
  }

  onChangeTaxonomyCatalog = (e) => {
    const value = e.target.value;
    const checked = e.target.checked;
    if (checked) {
      const schemeWithValue = _.filter(_.concat(this.state.scheme, value))
      this.setState({ 
        ... this.state,
        scheme: schemeWithValue
      });
    } else {
      const schemeWithoutValue = _.without(this.state.scheme, value);
      this.setState({ 
        ... this.state,
        scheme: schemeWithoutValue
      });
    }
  }

  onClickImportTaxonomyCatalogModal = (e) => {
    const { dispatch } = this.props;
    const { scheme } = this.state;
    if (!_.isEmpty(scheme)) {
      dispatch(importTaxonomyCatalog(scheme));
      this.setState({
        ...this.state,
        importTaxonomyCatalogOpen: false
      })
    }
  }

  onClickCloseCopyFromCurrentModal = (e) => {
    this.setState({
      ...this.state,
      copyFromCurrentOpen: false
    })
  }

  onClickCopyFromCurrentModal = (e) => {
    const { dispatch } = this.props;
    dispatch(copyFromCurrent());
    this.setState({
      ...this.state,
      copyFromCurrentOpen: false
    })
  }

  onClickCloseClearModal = (e) => {
    this.setState({
      ...this.state,
      clearOpen: false
    })
  }

  onClickClearModal = (e) => {
    const { dispatch } = this.props;
    dispatch(clear());
    this.setState({
      ...this.state,
      clearOpen: false
    })
  }

  onClickCloseCalculateContentImpactModal = (e) => {
    this.setState({
      ...this.state,
      calculateContentImpactOpen: false
    })
  }

  onClickCalculateContentImpactModal = (e) => {
    const { dispatch } = this.props;
    dispatch(calculateContentImpact());
    this.setState({
      ...this.state,
      calculateContentImpactOpen: false
    })
  }

  render() {

    const { initialVersionsRequestStarted, importTaxonomyFileRequestStarted, importTaxonomyCatalogRequestStarted, copyFromCurrentRequestStarted, calculateContentImpactRequestStarted, clearRequestStarted } = this.props;
    const { highlightedRowContext, importTaxonomyFileOpen, importTaxonomyCatalogOpen, file, copyFromCurrentOpen, clearOpen, calculateContentImpactOpen } = this.state;

    const head = this.createHead();
    const rows = this.createRows();

    const highlightedRowIndex = _.findIndex(rows, (row) => { return row.key == highlightedRowContext});

    return (
      <div>

        <DynamicTable
          head = { head }
          rows = { rows }
          rowsPerPage = { 5 }
          defaultPage = { 1 }
          loadingSpinnerSize = 'large'
          isFixedSize = { true }
          isRankable = { false }
          isLoading = { initialVersionsRequestStarted }
          highlightedRowIndex = { [highlightedRowIndex] }
        />
        <ModalTransition>

          { (importTaxonomyFileOpen || importTaxonomyFileRequestStarted) && (
            <Modal autoFocus = { false } onClose = { this.onClickCloseImportTaxonomyFileModal.bind(this) } isBlanketHidden = { true }>
              <ModalHeader>
                <ModalTitle>Import taxonomies and schemas from file</ModalTitle>
              </ModalHeader>
              <ModalBody>
              <div>
                <div>Select SKOS taxonomy or RDFS schema file to upload. RDF/XML, N-Triples, Turtle, Turtle-star, N3/Notation3, TriX, TriG, TriG-star, binary RDF, N-Quads, JSON-LD, RDF/JSON, RDFa and HDT formats are supported. The format is detected from the file extension.</div>
                <div className = 'ak-field-group'>
                  <input type = 'text' id = 'filename' name = 'filename' className = 'ak-field-text' placeholder = 'No file selected' value = { _.isEmpty(file) ? 'No file selected' : _.head(file)['name'] } readOnly required disabled = { importTaxonomyFileRequestStarted }/>
                </div>
                <div style = {{marginTop: '0.5em'}}>
                  <label htmlFor = 'taxonomy-upload' className = 'ak-button ak-button__appearance-default' disabled = { importTaxonomyFileRequestStarted }>Select file...</label>
                  <input id = 'taxonomy-upload' type = 'file' accept='.rdf,.rdfs,.owl,.xml,.nt,.ttl,.ttls,.n3,.trix,.trig,.trigs,.brf,.nq,.jsonld,.ndjsonld,.rj,.xhtml,.hdt' style = {{ display: 'none'}} onChange = { this.onTaxonomyFileChange.bind(this) } disabled = { importTaxonomyFileRequestStarted }/>
                </div>
              </div>
              </ModalBody>
              <ModalFooter>
                <Button appearance = 'subtle' isDisabled = { importTaxonomyFileRequestStarted } onClick = { this.onClickCloseImportTaxonomyFileModal.bind(this) }>
                  Cancel
                </Button>
                <LoadingButton appearance = 'primary' isLoading = { importTaxonomyFileRequestStarted } onClick = { this.onClickImportTaxonomyFileModal.bind(this) }>
                  Upload and import file
                </LoadingButton>
              </ModalFooter>
            </Modal>
          )}

          { (importTaxonomyCatalogOpen || importTaxonomyCatalogRequestStarted) && (
            <Modal autoFocus = { false } onClose = { this.onClickCloseImportTaxonomyCatalogModal.bind(this) } isBlanketHidden = { true }>
              <ModalHeader>
                <ModalTitle>Import taxonomies and schemas from catalog</ModalTitle>
              </ModalHeader>
              <ModalBody>
              <div>
                <div style = {{ paddingBottom: '1em' }}>Choose from the curated catalog of open-source SKOS taxonomies and RDFS schemas.</div>
                <div>
                  <div style = {{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'https://dalstonsemantics.com/ns/au/gov/abs/anzsic/scheme'
                      label = 'Australian and New Zealand Standard Industrial Classification (ANZSIC)'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href = 'https://github.com/cadmiumkitty/anzsic-taxonomy' target = '_blank'>
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'https://dalstonsemantics.com/ns/org/isbn-international/978-0124158290/scheme'
                      label = 'Taxonomy for Policy'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href = 'https://github.com/cadmiumkitty/policy-taxonomy' target = '_blank'>
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'https://dalstonsemantics.com/taxonomy/sdlc/scheme'
                      label = 'Software Development Lifecycle (SDLC) Document Types Taxonomy'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href="https://github.com/cadmiumkitty/sdlc-document-types-taxonomy" target="_blank">
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'https://dalstonsemantics.com/ns/org/opengroup/pubs/architecture/togaf9-doc/arch/chap37.html#scheme'
                      label = 'TOGAF® 9.2 Architecture Repository Document Types Taxonomy'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href = 'https://github.com/cadmiumkitty/togaf-architecture-repository-document-types-taxonomy' target = '_blank'>
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'http://www.semanticweb.org/ontologies/2020/4/VocabularyTOGAFContentMetamodel.skos#scheme'
                      label = 'TOGAF® 9.2 Content Metamodel Vocabulary'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href = 'https://github.com/cadmiumkitty/togaf-content-metamodel-ontology' target = '_blank'>
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                      label = 'The RDF Concepts Vocabulary (RDF)'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href = 'https://www.w3.org/TR/rdf-concepts/' target = '_blank'>
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'http://www.w3.org/2000/01/rdf-schema#'
                      label = 'The RDF Schema Vocabulary (RDFS)'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href = 'https://www.w3.org/TR/rdf-schema/' target = '_blank'>
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'https://schema.org/'
                      label = 'Schema.org'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href = 'https://schema.org/' target = '_blank'>
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center' }}>
                    <Checkbox
                      value = 'https://dalstonsemantics.com/dg/Schema'
                      label = 'Data Governance Schema and Taxonomy'
                      onChange = { this.onChangeTaxonomyCatalog.bind(this) } />
                    <a href = 'https://github.com/cadmiumkitty/data-governance' target = '_blank'>
                      <ShortcutIcon label = 'Open in new window' size = 'small' />
                    </a>
                  </div>
                </div>                
              </div>
              </ModalBody>
              <ModalFooter>
                <Button appearance = 'subtle' isDisabled = { importTaxonomyCatalogRequestStarted } onClick = { this.onClickCloseImportTaxonomyCatalogModal.bind(this) }>
                  Cancel
                </Button>
                <LoadingButton appearance = 'primary' isLoading = { importTaxonomyCatalogRequestStarted } onClick = { this.onClickImportTaxonomyCatalogModal.bind(this) }>
                  Import selected
                </LoadingButton>
              </ModalFooter>
            </Modal>
          )}

          { (copyFromCurrentOpen || copyFromCurrentRequestStarted) && (
            <Modal autoFocus = { false } onClose = { this.onClickCloseCopyFromCurrentModal.bind(this) } isBlanketHidden = { true }>
              <ModalHeader>
                <ModalTitle>Copy from current snapshot</ModalTitle>
              </ModalHeader>
              <ModalBody>Copy all Concept Schemes, Concepts, Classes and Properties from current snapshot to draft snapshot. Copying from current snapshot does not impact other snapshots or Confluence content.
              </ModalBody>
              <ModalFooter>
                <Button appearance = 'subtle' isDisabled = { copyFromCurrentRequestStarted } onClick = { this.onClickCloseCopyFromCurrentModal.bind(this) }>
                  Cancel
                </Button>
                <LoadingButton appearance = 'danger' isLoading = { copyFromCurrentRequestStarted } onClick = { this.onClickCopyFromCurrentModal.bind(this) }>
                  Copy from current snapshot
                </LoadingButton>
              </ModalFooter>
            </Modal>
          )}

          { (clearOpen || clearRequestStarted) && (
            <Modal autoFocus = { false } onClose = { this.onClickCloseClearModal.bind(this) } isBlanketHidden = { true }>
              <ModalHeader>
                <ModalTitle>Clear draft snapshot</ModalTitle>
              </ModalHeader>
              <ModalBody>Clear all Concept Schemes, Concepts, Classes and Properties from the draft snapshot. Clearing the draft snapshot does not impact other snapshots or Confluence content.
              </ModalBody>
              <ModalFooter>
                <Button appearance = 'subtle' isDisabled = { clearRequestStarted } onClick = { this.onClickCloseClearModal.bind(this) }>
                  Cancel
                </Button>
                <LoadingButton appearance = 'danger' isLoading = { clearRequestStarted } onClick = { this.onClickClearModal.bind(this) }>
                  Clear draft snapshot
                </LoadingButton>
              </ModalFooter>
            </Modal>
          )}

          { (calculateContentImpactOpen || calculateContentImpactRequestStarted) && (
            <Modal autoFocus = { false } onClose = { this.onClickCloseCalculateContentImpactModal.bind(this) } isBlanketHidden = { true }>
              <ModalHeader>
                <ModalTitle>Start switching to draft snapshot</ModalTitle>
              </ModalHeader>
              <ModalBody>Start switching to the draft snapshot. The switch can be completed or cancelled once Confluence content impacts are checked.
              </ModalBody>
              <ModalFooter>
                <Button appearance = 'subtle' isDisabled = { calculateContentImpactRequestStarted } onClick = { this.onClickCloseCalculateContentImpactModal.bind(this) }>
                  Cancel
                </Button>
                <LoadingButton appearance = 'primary' isLoading = { calculateContentImpactRequestStarted } onClick = { this.onClickCalculateContentImpactModal.bind(this) }>
                  Start switching
                </LoadingButton>
              </ModalFooter>
            </Modal>
          )}
        </ModalTransition>
      </div>
    );
  }
}

TaxonomyVersions.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { initialVersionsRequestStarted, importTaxonomyFileRequestStarted, importTaxonomyCatalogRequestStarted, copyFromCurrentRequestStarted, calculateContentImpactRequestStarted, transitionToCurrentRequestStarted, cancelTransitionToCurrentRequestStarted, clearRequestStarted, versions } = state.taxonomyVersions;
  return {
    initialVersionsRequestStarted,
    importTaxonomyFileRequestStarted,
    importTaxonomyCatalogRequestStarted,
    copyFromCurrentRequestStarted,
    calculateContentImpactRequestStarted,
    transitionToCurrentRequestStarted,
    cancelTransitionToCurrentRequestStarted,
    clearRequestStarted,
    versions
  };
}

export default connect(mapStateToProps)(TaxonomyVersions);
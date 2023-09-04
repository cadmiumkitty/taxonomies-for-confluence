import _ from 'lodash';

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import Button from '@atlaskit/button';
import { Checkbox } from '@atlaskit/checkbox';
import EmptyState from '@atlaskit/empty-state';
import EditorSearchIcon from '@atlaskit/icon/glyph/editor/search';
import { ObjectResult } from '@atlaskit/quick-search';
import Select from '@atlaskit/select';
import Spinner from '@atlaskit/spinner';
import TextField from '@atlaskit/textfield';

import { queryConcepts, resetConceptsQuery } from '../actions/Concept';
import { getParameters } from '../actions/Macro';
import { setTransitive, setPredicate, setObject, addPredicateObject, deletePredicateObject, tocMacroSaveMacro } from '../actions/TocMacro';

import ConceptLozenge from '../components/ConceptLozenge';

import { getId } from '../utils/Rdf';
import { getLabel } from '../utils/Rdfs';
import { getPrefLabel, getNotation, getInScheme, isConcept, isConceptScheme } from '../utils/Skos';
import { getSearchScore } from '../utils/Team';

class TocMacroEditor extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      q: ''
    };
  }

  componentDidMount = () => {
    const { dispatch } = this.props;
    AP.dialog.disableCloseOnSubmit();
    AP.events.on('dialog.submit', (data) => {
      dispatch(tocMacroSaveMacro());
      AP.events.offAll('dialog.submit');      
      AP.confluence.closeMacroEditor();
    });
    dispatch(getParameters());
  }

  debouncedDispatchQueryConcepts = _.debounce((dispatch, q) => { dispatch(queryConcepts(q)); }, 250);

  onChange = (e) => {
    const { dispatch } = this.props;
    const q = e.target.value;
    this.setState({q: q});
    if (_.size(q) > 0) {
      this.debouncedDispatchQueryConcepts(dispatch, q);
    } else {
      this.debouncedDispatchQueryConcepts.cancel();
      dispatch(resetConceptsQuery());
    }
  }

  renderStartupProgress = () => {
    return (
      <div style = {{ height: '26px', paddingBottom: '1em' }}>
        <Spinner />
      </div>
    );
  }

  onDeleteClick = (index, e) => {
    const { dispatch } = this.props;
    dispatch(deletePredicateObject(index));    
  }

  renderQueryBuilderAdded = () => {
    const { predicateObject } = this.props;
    return (
      <div style = {{ paddingBottom: '2em' }}>
        { 
          _.map(predicateObject, (po, index) => {
            return (
              <div key = { `predicate-object-${index}` } style = {{ paddingBottom: '4px' }}>
                <div style = {{ width: '10%', display: 'inline-block' }}>{ po.predicateUri == 'http://purl.org/dc/terms/type' ? 'Type' : po.predicateUri == 'http://purl.org/dc/terms/subject' ? 'Subject' : 'Relation' }</div>
                <div style = {{ width: '20%', display: 'inline-block' }}>{ po.transitive ? 'Including narrower' : <div/> }</div>
                <div style = {{ width: '55%', display: 'inline-flex' }}>{ po.error ? <ConceptLozenge predicate = { po.predicateUri } appearance = 'error' label = 'Something went wrong looking up the concept' /> : <ConceptLozenge predicate = { po.predicateUri } appearance = 'default' label = { getPrefLabel(po.objectConcept) } /> }<div style = {{paddingLeft: '8px'}}></div></div>
                <div style = {{ width: '15%', display: 'inline-block'}}><Button style = {{ width: '100%' }} onClick = { this.onDeleteClick.bind(this, index) } >Delete</Button></div>
              </div>
            );
          }) 
        }
      </div>
    );    
  }

  onPredicateChange = (e) => {
    const { dispatch } = this.props;
    const predicate = e.value;
    dispatch(setPredicate(predicate));    
  }

  onTransitiveChange = (e) => {
    const { dispatch } = this.props;
    const transitive = e.target.checked;
    dispatch(setTransitive(transitive));    
  }

  onAddClick = (e) => {
    const { dispatch } = this.props;
    const transitive = e.target.checked;
    dispatch(addPredicateObject());    
  }

  renderQueryBuilderToAdd = () => {
    const { tocMacroStartingUp, predicateObject, transitive, predicateUri, objectConcept } = this.props;
    return (
      <div>
        <div style = {{ paddingBottom: '2px' }}>
          <div style = {{ width: '35%', display: 'inline-block' }}>
            <Select
              inputId = 'predicate'
              selectedValue = { predicateUri }
              defaultValue = {{ label: 'Relation', value: 'http://purl.org/dc/terms/relation' }}
              className = 'single-select'
              classNamePrefix = 'react-select'
              options = {[
                { label: 'Type', value: 'http://purl.org/dc/terms/type' },
                { label: 'Subject', value: 'http://purl.org/dc/terms/subject' },
                { label: 'Relation', value: 'http://purl.org/dc/terms/relation' }
              ]}
              onChange = { this.onPredicateChange.bind(this)}
              spacing = 'compact'
              isDisabled = { tocMacroStartingUp } />
          </div>
          <div style = {{ width: '50%', display: 'inline-block' }}></div>
          <div style = {{ width: '15%', display: 'inline-block' }}>
            <Button style = {{ width: '100%' }} onClick = { this.onAddClick.bind(this) } isDisabled = { tocMacroStartingUp || _.size(predicateObject) >= 5 || !objectConcept } >Add</Button>
          </div>
        </div>
        <div style = {{ paddingBottom: '8px' }}>
          <div style = {{ display: 'inline-block' }}>
            <Checkbox label = 'Include narrower concepts' name = { 'transitive' } isChecked = { transitive } onChange = { this.onTransitiveChange.bind(this) } isDisabled = { tocMacroStartingUp } />
          </div>  
        </div>
        <div style = {{ paddingBottom: '1em' }}>
          <div style = {{ display: 'inline-flex' }}>
            { objectConcept ? <ConceptLozenge predicate = { predicateUri } appearance = 'default' label = { getPrefLabel(objectConcept) } /> : <ConceptLozenge predicate = { predicateUri } appearance = 'empty' label = 'No concept selected' /> }
          </div>
        </div>
      </div>
    );
  }

  renderConceptSearchBar = () => {
    const { tocMacroStartingUp } = this.props;
    return (
      <TextField onChange = { this.onChange.bind(this) } elemBeforeInput = { <EditorSearchIcon/> } isCompact = { true } placeholder = 'Start searching for a concept'  autoFocus = { true } isDisabled = { tocMacroStartingUp } />
    );
  }

  renderQueryProgress = () => {
    return (
      <div style = {{ marginTop: '1em' }}>
        <Spinner />
      </div>
    );
  }

  renderNoResults = () => {
    return (
      <EmptyState 
        header = 'Could not find any concept matching your search.'
        description = 'Try again with a different search.'
      />
    );
  }

  renderQueryError = () => {
    const { conceptsQueryErrorDescription } = this.props;
    return (
      <EmptyState 
        header = 'Something went wrong when we were looking up concepts matching your search.'
        description = { conceptsQueryErrorDescription }
      />
    )
  }

  renderResultsEmpty = () => {
    return (
      <EmptyState 
        header = 'Look for a concept to add to the criteria.'
        description = 'Start searching for a concept based on label or notation. Select the concept to add to the criteria. Choose whether the concept is the subject or the relation of a page or a blog. Choose whether narrower concepts should be included. Click Add to add up to 5 concepts to the table of contents criteria.'
      />
    );
  }

  onClick = (uri, concept, scheme, e) => {
    const { dispatch } = this.props;
    dispatch(setObject(uri, concept, scheme));
  }

  renderResults = () => {
    const { results } = this.props;
    const schemes = _.keyBy(_.filter(results, isConceptScheme), '@id');
    return (
      <div style = {{ marginTop: '1em' }}>
        {_.map(_.take(_.reverse(_.sortBy(_.filter(results, isConcept), getSearchScore)), 5), result => {
          const scheme = schemes[getInScheme(result)];
          const containerName = _.isUndefined(scheme)
              ? undefined
              : _.isUndefined(getPrefLabel(scheme)) 
                  ? _.isUndefined(getLabel(scheme))
                      ? undefined
                      : getLabel(scheme)
                  : getPrefLabel(scheme);
          return ( 
            <ObjectResult
              key = { getId(result) }
              resultId = { getId(result) }
              avatar = { <img src = 'icons/concept.svg' height = '24px' width = '24px' /> }
              name = { getPrefLabel(result) }
              objectKey = { _.head(getNotation(result)) }
              containerName = { containerName }
              onClick = { this.onClick.bind(this, getId(result), result, scheme) }
            />
          );
        })}
      </div>
    );
  }

  render() {
    const { conceptsQueryStarted, conceptsQueryError, results, tocMacroStartingUp, predicateObject } = this.props;
    const { q } = this.state;
    return (
      <div>
        { tocMacroStartingUp 
            ? this.renderStartupProgress()
            : _.size(predicateObject) > 0 
                ? this.renderQueryBuilderAdded() 
                : <div/> }
        { this.renderQueryBuilderToAdd() }
        { this.renderConceptSearchBar() }
        { conceptsQueryStarted 
            ? this.renderQueryProgress() 
            : conceptsQueryError 
                ? this.renderQueryError() 
                : _.isEmpty(q) 
                    ? this.renderResultsEmpty() 
                    : _.size(results) > 0 
                        ? this.renderResults() 
                        : this.renderNoResults() }        
      </div>
    );
  }
}

TocMacroEditor.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { conceptsQueryStarted, conceptsQueryError, conceptsQueryErrorDescription, results } = state.concept;
  const { tocMacroStartingUp, predicateObject, transitive, predicateUri, objectUri, objectConcept, objectScheme } = state.toc;

  return {
    conceptsQueryStarted,
    conceptsQueryError,
    conceptsQueryErrorDescription,
    results,
    tocMacroStartingUp,
    predicateObject,
    transitive,
    predicateUri,
    objectUri,
    objectConcept,
    objectScheme
  };
}

export default connect(mapStateToProps)(TocMacroEditor);
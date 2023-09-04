import _ from 'lodash';

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import EmptyState from '@atlaskit/empty-state';
import { ObjectResult } from '@atlaskit/quick-search';
import EditorSearchIcon from '@atlaskit/icon/glyph/editor/search';
import Spinner from '@atlaskit/spinner';
import TextField from '@atlaskit/textfield';

import { queryConcepts, resetConceptsQuery } from '../actions/Concept';
import { getParameters } from '../actions/Macro';
import { setRelation, relationMacroSaveMacro } from '../actions/RelationMacro';

import ConceptLozenge from '../components/ConceptLozenge';

import { getPrefLabel, getNotation, getInScheme, isConcept, isConceptScheme } from '../utils/Skos';
import { getId } from '../utils/Rdf';
import { getLabel } from '../utils/Rdfs';
import { getSearchScore } from '../utils/Team';

class RelationMacroEditor extends React.Component {

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
      dispatch(relationMacroSaveMacro());
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

  renderConceptSearchBar = () => {
    const { relationMacroStartingUp } = this.props;
    return (
      <div style = {{ marginTop: '1em' }}>
        <TextField onChange = { this.onChange.bind(this) } elemBeforeInput = { <EditorSearchIcon/> } isCompact = { true } placeholder = 'Start searching for a concept' autoFocus = { true } isDisabled = { relationMacroStartingUp }/>
      </div>
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
        description = 'Try again with a different search query.'
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
        header = 'Look for a concept related to this page.'
        description = 'Start searching for a concept based on label or notation. Select a concept related to this page.'
      />
    );
  }

  onClick = (uri, concept, scheme, e) => {
    const { dispatch } = this.props;
    dispatch(setRelation(uri, concept, scheme));
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
    const { conceptsQueryStarted, conceptsQueryError, results, relationMacroStartingUp, relationError, concept } = this.props;
    const { q } = this.state;
    return (
      <div>
        { relationMacroStartingUp 
            ? this.renderStartupProgress()
            : relationError
                ? <ConceptLozenge predicate = 'http://purl.org/dc/terms/relation' appearance = 'error' label = 'Something went wrong looking up the related concept' />
                : _.isEmpty(concept)
                    ? <ConceptLozenge predicate = 'http://purl.org/dc/terms/relation' appearance = 'empty' label = 'No related concept selected' />
                    : <ConceptLozenge predicate = 'http://purl.org/dc/terms/relation' appearance = 'default' label = { getPrefLabel(concept) } /> }
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

RelationMacroEditor.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { conceptsQueryStarted, conceptsQueryError, conceptsQueryErrorDescription, results } = state.concept;
  const { relationMacroStartingUp, error: relationError, concept, scheme } = state.relationMacro;

  return {
    conceptsQueryStarted,
    conceptsQueryError,
    conceptsQueryErrorDescription,
    results,
    relationMacroStartingUp,
    relationError,
    concept,
    scheme
  };
}

export default connect(mapStateToProps)(RelationMacroEditor);
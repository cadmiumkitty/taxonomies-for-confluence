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
import { getContentProperty, setContentProperty } from '../actions/Content';

import { getId } from '../utils/Rdf';
import { getLabel } from '../utils/Rdfs';
import { getPrefLabel, getNotation, getInScheme, isConcept, isConceptScheme } from '../utils/Skos';
import { getSearchScore } from '../utils/Team';

class TypeByline extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      q: ''
    };
  }

  componentDidMount = () => {
    const { dispatch } = this.props;
    dispatch(getContentProperty('taxonomies-for-confluence-type'));
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

  renderConceptSearchBar = () => {
    return (
      <TextField onChange = { this.onChange.bind(this) } elemBeforeInput = { <EditorSearchIcon/> } isCompact = { true } placeholder = 'Start searching for a concept'  autoFocus = { true } />
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

  renderResultsError = () => {
    const { conceptQueryErrorDescription } = this.props;
    return (
      <EmptyState 
        header = 'Something went wrong when we were looking up concepts matching your search.'
        description = { conceptQueryErrorDescription }
      />
    )
  }

  renderResultsEmpty = () => {
    return (
      <EmptyState 
        header = 'Look for a concept to set as the type of this page.'
        description = 'Start searching for a concept based on label or notation. Select the concept that reflects what this page is.'
      />
    );
  }

  onClick = (result, e) => {
    const { dispatch, type } = this.props;
    const typeUpdate = {
      key: 'taxonomies-for-confluence-type',
      value: {
        uri: getId(result),
        notation: _.head(getNotation(result)),
        name: {
          value: getPrefLabel(result)
        },
        tooltip: {
          value: _.join(_.compact([_.head(getNotation(result)), getPrefLabel(result)]), ' ')
        },
        icon: {
          width: 16,
          height: 16,
          url: 'icons/byline-type-01.png'
        }
      },
      version: {
        number: type ? type.version.number + 1 : 1
      }
    };
    dispatch(setContentProperty(typeUpdate));
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
              onClick = { this.onClick.bind(this, result) }
            />
          );
        })}
      </div>
    );
  }

  render() {
    const { conceptsQueryStarted, conceptsQueryError, results } = this.props;
    const { q } = this.state;
    return (
      <div>
        { this.renderConceptSearchBar() }
        { conceptsQueryStarted 
            ? this.renderQueryProgress() 
            : conceptsQueryError 
                ? this.renderResultsError() 
                : _.isEmpty(q) 
                    ? this.renderResultsEmpty() 
                    : _.size(results) > 0 
                        ? this.renderResults() 
                        : this.renderNoResults() }
      </div>
    );
  }
}

TypeByline.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { conceptsQueryStarted, conceptsQueryError, conceptsQueryErrorDescription, results } = state.concept;
  const type = state.content.properties['taxonomies-for-confluence-type'];

  return {
    conceptsQueryStarted,
    conceptsQueryError,
    conceptsQueryErrorDescription,
    results,
    type
  };
}

export default connect(mapStateToProps)(TypeByline);
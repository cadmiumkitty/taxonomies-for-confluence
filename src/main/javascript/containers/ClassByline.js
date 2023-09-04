import _ from 'lodash';

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import EmptyState from '@atlaskit/empty-state';
import { ObjectResult } from '@atlaskit/quick-search';
import EditorSearchIcon from '@atlaskit/icon/glyph/editor/search';
import Spinner from '@atlaskit/spinner';
import TextField from '@atlaskit/textfield';

import { queryRdfsClasses, resetRdfsClassesQuery } from '../actions/RdfsClass';
import { getContentProperty, setContentProperty } from '../actions/Content';

import { getId } from '../utils/Rdf';
import { getLabel, getIsDefinedBy, isClass } from '../utils/Rdfs';
import { getSearchScore, isDefiningResource } from '../utils/Team';

class ClassByline extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      q: ''
    };
  }

  componentDidMount = () => {
    const { dispatch } = this.props;
    dispatch(getContentProperty('taxonomies-for-confluence-class'));
  }

  debouncedDispatchQueryRdfsClasses = _.debounce((dispatch, q) => { dispatch(queryRdfsClasses(q)); }, 250);

  onChange = (e) => {
    const { dispatch } = this.props;
    const q = e.target.value;
    this.setState({q: q});
    if (_.size(q) > 0) {
      this.debouncedDispatchQueryRdfsClasses(dispatch, q);
    } else {
      this.debouncedDispatchQueryRdfsClasses.cancel();
      dispatch(resetRdfsClassesQuery());
    }
  }

  renderRdfsClassSearchBar = () => {
    return (
      <TextField onChange = { this.onChange.bind(this) } elemBeforeInput = { <EditorSearchIcon/> } isCompact = { true } placeholder = 'Start searching for a class'  autoFocus = { true } />
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
        header = 'Could not find any classes matching your search.'
        description = 'Try again with a different search.'
      />
    );
  }

  renderResultsError = () => {
    const { rdfsClassQueryErrorDescription } = this.props;
    return (
      <EmptyState 
        header = 'Something went wrong when we were looking up classes matching your search.'
        description = { rdfsClassQueryErrorDescription }
      />
    )
  }

  renderResultsEmpty = () => {
    return (
      <EmptyState 
        header = 'Look for a class to set as the class of this page.'
        description = 'Start searching for a class based on label. Select the class of this page.'
      />
    );
  }

  onClick = (result, e) => {
    const { dispatch, rdfsClass } = this.props;
    const rdfsClassUpdate = {
      key: 'taxonomies-for-confluence-class',
      value: {
        uri: getId(result),
        name: {
          value: getLabel(result)
        },
        tooltip: {
          value: _.join(_.compact([getLabel(result)]), ' ')
        },
        icon: {
          width: 16,
          height: 16,
          url: 'icons/byline-class-01.png'
        }
      },
      version: {
        number: rdfsClass ? rdfsClass.version.number + 1 : 1
      }
    };
    dispatch(setContentProperty(rdfsClassUpdate));
  }

  renderResults = () => {
    const { results } = this.props;
    const definingResources = _.keyBy(_.filter(results, isDefiningResource), '@id');
    return (
      <div style = {{ marginTop: '1em' }}>
        {_.map(_.take(_.reverse(_.sortBy(_.filter(results, isClass), getSearchScore)), 5), result => {
          const definingResource = definingResources[getIsDefinedBy(result)];
          const containerName = _.isUndefined(definingResource)
              ? undefined
              : _.isUndefined(getLabel(definingResource))
                      ? undefined
                      : getLabel(definingResource);
          return ( 
            <ObjectResult
              key = { getId(result) }
              resultId = { getId(result) }
              avatar = { <img src = 'icons/class.svg' height = '24px' width = '24px' /> }
              name = { getLabel(result) }
              containerName = { containerName }
              onClick = { this.onClick.bind(this, result) }
            />
          );
        })}
      </div>
    );
  }

  render() {
    const { rdfsClassesQueryStarted, rdfsClassesQueryError, results } = this.props;
    const { q } = this.state;
    return (
      <div>
        { this.renderRdfsClassSearchBar() }
        { rdfsClassesQueryStarted 
            ? this.renderQueryProgress() 
            : rdfsClassesQueryError 
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

ClassByline.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { rdfsClassesQueryStarted, rdfsClassesQueryError, rdfsClassesQueryErrorDescription, results } = state.rdfsClass;
  const rdfsClass = state.content.properties['taxonomies-for-confluence-class'];

  return {
    rdfsClassesQueryStarted,
    rdfsClassesQueryError,
    rdfsClassesQueryErrorDescription,
    results,
    rdfsClass
  };
}

export default connect(mapStateToProps)(ClassByline);
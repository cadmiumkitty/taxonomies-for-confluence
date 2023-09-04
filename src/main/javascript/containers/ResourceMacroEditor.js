import _ from 'lodash';

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import EmptyState from '@atlaskit/empty-state';
import { ObjectResult } from '@atlaskit/quick-search';
import EditorSearchIcon from '@atlaskit/icon/glyph/editor/search';
import Spinner from '@atlaskit/spinner';
import TextField from '@atlaskit/textfield';

import { queryResources, resetResourcesQuery } from '../actions/Resource';
import { getParameters } from '../actions/Macro';
import { setResource, resourceMacroSaveMacro } from '../actions/ResourceMacro';

import ResourceLozenge from '../components/ResourceLozenge';

import { getNotation, getInScheme, isConcept, isConceptScheme } from '../utils/Skos';
import { getId, getType, isProperty } from '../utils/Rdf';
import { getIsDefinedBy, isClass } from '../utils/Rdfs';
import { getSearchScore } from '../utils/Team';
import { getLabel } from '../utils/Label';

class ResourceMacroEditor extends React.Component {

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
      dispatch(resourceMacroSaveMacro());
      AP.events.offAll('dialog.submit');      
      AP.confluence.closeMacroEditor();
    });
    dispatch(getParameters());
  }

  debouncedDispatchQueryResurces = _.debounce((dispatch, q) => { dispatch(queryResources(q)); }, 250);

  onChange = (e) => {
    const { dispatch } = this.props;
    const q = e.target.value;
    this.setState({q: q});
    if (_.size(q) > 0) {
      this.debouncedDispatchQueryResurces(dispatch, q);
    } else {
      this.debouncedDispatchQueryResurces.cancel();
      dispatch(resetResourcesQuery());
    }
  }

  renderStartupProgress = () => {
    return (
      <div style = {{ height: '26px', paddingBottom: '1em' }}>
        <Spinner />
      </div>
    );
  }

  renderResourceSearchBar = () => {
    const { resourceMacroStartingUp } = this.props;
    return (
      <div style = {{ marginTop: '1em' }}>
        <TextField onChange = { this.onChange.bind(this) } elemBeforeInput = { <EditorSearchIcon/> } isCompact = { true } placeholder = 'Start searching for a resource' autoFocus = { true } isDisabled = { resourceMacroStartingUp }/>
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
        header = 'Could not find any resources matching your search.'
        description = 'Try again with a different search query.'
      />
    );
  }

  renderQueryError = () => {
    const { resourcesQueryErrorDescription } = this.props;
    return (
      <EmptyState 
        header = 'Something went wrong when we were looking up resources matching your search.'
        description = { resourcesQueryErrorDescription }
      />
    )
  }

  renderResultsEmpty = () => {
    return (
      <EmptyState 
        header = 'Look for a resource to insert into this page.'
        description = 'Start searching for a resource based on label. Select a resource to insert into to this page.'
      />
    );
  }

  onClick = (uri, resource, e) => {
    const { dispatch } = this.props;
    dispatch(setResource(uri, resource));
  }

  renderResults = () => {
    const { results } = this.props;
    const schemes = _.keyBy(_.filter(results, isConceptScheme), '@id');
    return (
      <div style = {{ marginTop: '1em' }}>
        {_.map(_.take(_.reverse(_.sortBy(_.filter(results, (r) => {return isClass(r) || isProperty(r) || isConcept(r)}), getSearchScore)), 5), result => {
          const label = getLabel(result);
          const scheme = schemes[getInScheme(result)];
          const schemeLabel = getLabel(scheme);
          const isDefinedBy = getIsDefinedBy(result);
          const definingResource = _.head(_.filter(results, (r) => getId(r) == isDefinedBy));
          const definingResourceLabel = getLabel(definingResource);
          const containerName = _.isUndefined(definingResourceLabel)
            ? _.isUndefined(schemeLabel)
              ? undefined
              : schemeLabel
            : definingResourceLabel;
          const resultType = getType(result);
          // We can only return class, property or concept, so can simplify the check and reduce number of icons we need
          const icon = resultType == 'http://www.w3.org/2000/01/rdf-schema#Class'
            ? 'icons/class.svg'
            : resultType == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#Property'
              ? 'icons/property.svg'
              : 'icons/concept.svg' ;
          return ( 
            <ObjectResult
              key = { getId(result) }
              resultId = { getId(result) }
              avatar = { <img src = { icon } height = '24px' width = '24px' /> }
              name = { label }
              objectKey = { _.head(getNotation(result)) }
              containerName = { containerName }
              onClick = { this.onClick.bind(this, getId(result), result) }
            />
          );
        })}
      </div>
    );
  }

  render() {
    const { resourcesQueryStarted, resourcesQueryError, results, resourceMacroStartingUp, resourceError, resource } = this.props;
    const { q } = this.state;
    return (
      <div>
        { resourceMacroStartingUp 
            ? this.renderStartupProgress()
            : resourceError
                ? <ResourceLozenge appearance = 'error' label = 'Something went wrong looking up the resource' />
                : _.isEmpty(resource)
                    ? <ResourceLozenge appearance = 'empty' label = 'No resource selected' />
                    : <ResourceLozenge appearance = 'default' label = { getLabel(resource) } type = { getType(resource) } /> }
        { this.renderResourceSearchBar() }
        { resourcesQueryStarted 
            ? this.renderQueryProgress() 
            : resourcesQueryError 
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

ResourceMacroEditor.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { resourcesQueryStarted, resourcesQueryError, resourcesQueryErrorDescription, results } = state.resource;
  const { resourceMacroStartingUp, error: resourceError, resource, isDefinedBy } = state.resourceMacro;

  return {
    resourcesQueryStarted,
    resourcesQueryError,
    resourcesQueryErrorDescription,
    results,
    resourceMacroStartingUp,
    resourceError,
    resource,
    isDefinedBy
  };
}

export default connect(mapStateToProps)(ResourceMacroEditor);
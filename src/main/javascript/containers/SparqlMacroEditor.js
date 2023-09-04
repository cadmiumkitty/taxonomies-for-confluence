import _ from 'lodash';

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import SectionMessage, { SectionMessageAction } from '@atlaskit/section-message';
import Spinner from '@atlaskit/spinner';

import '@rdfjs-elements/sparql-editor';

import { getParameters } from '../actions/Macro';
import { sparqlMacroSaveMacro, setSparqlQuery } from '../actions/SparqlMacro';

class SparqlMacroEditor extends React.Component {

  constructor(props) {
    super(props);
    this.sparqlEditorRef = React.createRef();
  }

  onSetExampleOne = (e) => {
    const query = `PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX anzsic: <https://dalstonsemantics.com/ns/au/gov/abs/anzsic/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dcterms: <http://purl.org/dc/terms/>

SELECT ?page ?title 
WHERE {
  ?page a team:page ;
    rdfs:label ?title ;
    dcterms:related anzsic:6222 .
}`;
    const { dispatch } = this.props;
    dispatch(setSparqlQuery(query));
  }

  onSetExampleTwo = (e) => {
    const query = `PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX togaf: <http://www.semanticweb.org/ontologies/2020/4/OntologyTOGAFContentMetamodel.owl#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?process_name ?data_entity_name
WHERE {
  ?process a togaf:Process ;
    rdfs:label ?process_name ;
    togaf:processDecomposesAndOrOrchestratesBusinessService ?business_service .
  ?data_entity a togaf:DataEntity ;
    rdfs:label ?data_entity_name ;
    togaf:dataEntityIsAccessedAndUpdatedThroughBusinessService ?business_service .
}`;
    const { dispatch } = this.props;
    dispatch(setSparqlQuery(query));
  }

  onSetExampleThree = (e) => {
    const query = `PREFIX lrppi: <http://landregistry.data.gov.uk/def/ppi/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX lrcommon: <http://landregistry.data.gov.uk/def/common/>
PREFIX sdo: <https://schema.org/>

SELECT ?date (?paon AS ?primary_address) (?saon AS ?secondary_address) ?amount
WHERE {
  { SELECT ?postcode WHERE { ?this sdo:postalCode ?postcode . } }
  SERVICE <https://landregistry.data.gov.uk/landregistry/query> { 
    ?addr lrcommon:postcode ?postcode.
    ?transx lrppi:propertyAddress ?addr ;
            lrppi:pricePaid ?amount ;
            lrppi:transactionDate ?date ;
            lrppi:transactionCategory/skos:prefLabel ?category.
    OPTIONAL {?addr lrcommon:paon ?paon}
    OPTIONAL {?addr lrcommon:saon ?saon}
  }
}
ORDER BY DESC(?date)
LIMIT 5
`;
    const { dispatch } = this.props;
    dispatch(setSparqlQuery(query));
}

  onSetExampleFour = (e) => {
    const query = `PREFIX wd: <http://www.wikidata.org/entity/>
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
PREFIX wikibase: <http://wikiba.se/ontology#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX bd: <http://www.bigdata.com/rdf#>

SELECT ?cat ?label
WHERE 
{
  SERVICE <https://query.wikidata.org/bigdata/namespace/wdq/sparql> {
  	?cat wdt:P31 wd:Q146.
  	SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en" . 
    	?cat rdfs:label ?label .}
  }
}`;
    const { dispatch } = this.props;
    dispatch(setSparqlQuery(query));
}

  componentDidMount = () => {
    const { dispatch } = this.props;
    AP.dialog.disableCloseOnSubmit();
    AP.events.on('dialog.submit', (data) => {
      dispatch(setSparqlQuery(this.sparqlEditorRef.current.value));
      dispatch(sparqlMacroSaveMacro());
      AP.events.offAll('dialog.submit');      
      AP.confluence.closeMacroEditor();
    });
    dispatch(getParameters());
  }

  render() {
    const { query } = this.props;
    return (
      <div>
        <SectionMessage
            title="Restricted set of SPARQL operations is allowed"
            actions={[
              <SectionMessageAction href="#" onClick = { this.onSetExampleOne.bind(this) }>All pages related to ANZSIC Building Society Operation</SectionMessageAction>,
              <SectionMessageAction href="#" onClick = { this.onSetExampleTwo.bind(this) }>All TOGAF Processes and Data Entities that they use</SectionMessageAction>,
              <SectionMessageAction href="#" onClick = { this.onSetExampleThree.bind(this) }>All sales in a UK Schema.org postal code</SectionMessageAction>,
              <SectionMessageAction href="#" onClick = { this.onSetExampleFour.bind(this) }>All cats in Wikidata</SectionMessageAction>,
              ]}>
          <p>Only the SELECT query form is allowed. The SERVICE keyword is supported and you can make calls to external SPARQL endpoints, however execution time is limited to 5 seconds. Binding for "this" is this resource URI. You can't use update operations. Use one of our examples to get started.</p>
        </SectionMessage>
        <div style = {{ marginTop: '1em' }}>
          <sparql-editor value = { query } auto-parse = { true } ref = { this.sparqlEditorRef }></sparql-editor>
        </div>
      </div>
    );
  }
}

SparqlMacroEditor.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  const { query } = state.sparql;

  return {
    query
  };
}

export default connect(mapStateToProps)(SparqlMacroEditor);
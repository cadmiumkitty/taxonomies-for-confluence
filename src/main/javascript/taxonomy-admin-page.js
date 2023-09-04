import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import '@atlaskit/css-reset';
import '@atlaskit/reduced-ui-pack';

import { taxonomy } from './reducers/Taxonomy';
import { schemaClass } from './reducers/SchemaClass';
import { schemaProperty } from './reducers/SchemaProperty';
import { taxonomyVersions } from './reducers/TaxonomyVersions';

import { createTaxonomyMiddleware } from './middleware/Taxonomy';
import { createSchemaClassMiddleware } from './middleware/SchemaClass';
import { createSchemaPropertyMiddleware } from './middleware/SchemaProperty';
import { createTaxonomyVersionsMiddleware } from './middleware/TaxonomyVersions';

import TaxonomyAdminPage from './containers/TaxonomyAdminPage';

const reducer = combineReducers({
	taxonomy,
	schemaClass,
	schemaProperty,
	taxonomyVersions
});

const taxonomyMiddleware = createTaxonomyMiddleware();
const schemaClassMiddleware = createSchemaClassMiddleware();
const schemaPropertyMiddleware = createSchemaPropertyMiddleware();
const taxonomyVersionsMiddleware = createTaxonomyVersionsMiddleware();

const middleware = applyMiddleware(thunk, taxonomyMiddleware, schemaClassMiddleware, schemaPropertyMiddleware, taxonomyVersionsMiddleware);

const store = createStore(reducer, middleware);

ReactDOM.render(
	<Provider store={store}>
		<TaxonomyAdminPage />
	</Provider>,
	document.getElementById('ac-content')
);
import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import '@atlaskit/css-reset';
import '@atlaskit/reduced-ui-pack';

import { taxonomy } from './reducers/Taxonomy';

import { createTaxonomyMiddleware } from './middleware/Taxonomy';

import TaxonomyPage from './containers/TaxonomyPage';

const reducer = combineReducers({
	taxonomy
});

const taxonomyMiddleware = createTaxonomyMiddleware();

const middleware = applyMiddleware(thunk, taxonomyMiddleware);

const store = createStore(reducer, middleware);

ReactDOM.render(
	<Provider store={store}>
		<TaxonomyPage />
	</Provider>,
	document.getElementById('ac-content')
);
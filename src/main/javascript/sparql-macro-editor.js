import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import '@atlaskit/css-reset';
import '@atlaskit/reduced-ui-pack';

import { macro } from './reducers/Macro';
import { sparql } from './reducers/SparqlMacro';

import { createMacroMiddleware } from './middleware/Macro';
import { createSparqlMacroMiddleware } from './middleware/SparqlMacro';

import SparqlMacroEditor from './containers/SparqlMacroEditor';

const reducer = combineReducers({
    macro,
    sparql
});

const macroMiddleware = createMacroMiddleware();
const sparqlMacroMiddleware = createSparqlMacroMiddleware();

const middleware = applyMiddleware(thunk, sparqlMacroMiddleware, macroMiddleware );

const store = createStore(reducer, middleware);

ReactDOM.render(
	<Provider store={store}>
		<SparqlMacroEditor />
	</Provider>,
	document.getElementById('sparql-macro-editor')
);
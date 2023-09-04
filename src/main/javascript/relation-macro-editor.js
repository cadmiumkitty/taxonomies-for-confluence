import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import '@atlaskit/css-reset';
import '@atlaskit/reduced-ui-pack';

import { concept } from './reducers/Concept';
import { macro } from './reducers/Macro';
import { relationMacro } from './reducers/RelationMacro';

import { createConceptMiddleware } from './middleware/Concept';
import { createMacroMiddleware } from './middleware/Macro';
import { createRelationMacroMiddleware } from './middleware/RelationMacro';

import RelationMacroEditor from './containers/RelationMacroEditor';

const reducer = combineReducers({
	concept,
    macro,
    relationMacro
});

const conceptMiddleware = createConceptMiddleware();
const macroMiddleware = createMacroMiddleware();
const relationMacroMiddleware = createRelationMacroMiddleware();

const middleware = applyMiddleware(thunk, conceptMiddleware, relationMacroMiddleware, macroMiddleware);

const store = createStore(reducer, middleware);

ReactDOM.render(
	<Provider store={store}>
		<RelationMacroEditor />
	</Provider>,
	document.getElementById('relation-macro-editor')
);
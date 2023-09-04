import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import '@atlaskit/css-reset';
import '@atlaskit/reduced-ui-pack';

import { concept } from './reducers/Concept';
import { content } from './reducers/Content';

import { createConceptMiddleware } from './middleware/Concept';
import { createContentMiddleware } from './middleware/Content';

import TypeByline from './containers/TypeByline';

const reducer = combineReducers({
	concept,
	content
});

const conceptMiddleware = createConceptMiddleware();
const contentMiddleware = createContentMiddleware();

const middleware = applyMiddleware(thunk, conceptMiddleware, contentMiddleware);

const store = createStore(reducer, middleware);

ReactDOM.render(
	<Provider store={store}>
		<TypeByline />
	</Provider>,
	document.getElementById('type-byline')
);
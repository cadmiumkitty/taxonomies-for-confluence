import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import '@atlaskit/css-reset';
import '@atlaskit/reduced-ui-pack';

import { rdfsClass } from './reducers/RdfsClass';
import { content } from './reducers/Content';

import { createRdfsClassMiddleware } from './middleware/RdfsClass';
import { createContentMiddleware } from './middleware/Content';

import ClassByline from './containers/ClassByline';

const reducer = combineReducers({
	rdfsClass,
	content
});

const rdfsClassMiddleware = createRdfsClassMiddleware();
const contentMiddleware = createContentMiddleware();

const middleware = applyMiddleware(thunk, rdfsClassMiddleware, contentMiddleware);

const store = createStore(reducer, middleware);

ReactDOM.render(
	<Provider store={store}>
		<ClassByline />
	</Provider>,
	document.getElementById('class-byline')
);
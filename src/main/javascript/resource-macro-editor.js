import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import '@atlaskit/css-reset';
import '@atlaskit/reduced-ui-pack';

import { resource } from './reducers/Resource';
import { macro } from './reducers/Macro';
import { resourceMacro } from './reducers/ResourceMacro';

import { createResourceMiddleware } from './middleware/Resource';
import { createMacroMiddleware } from './middleware/Macro';
import { createResourceMacroMiddleware } from './middleware/ResourceMacro';

import ResourceMacroEditor from './containers/ResourceMacroEditor';

const reducer = combineReducers({
	resource,
    macro,
    resourceMacro
});

const resourceMiddleware = createResourceMiddleware();
const macroMiddleware = createMacroMiddleware();
const resourceMacroMiddleware = createResourceMacroMiddleware();

const middleware = applyMiddleware(thunk, resourceMiddleware, resourceMacroMiddleware, macroMiddleware);

const store = createStore(reducer, middleware);

ReactDOM.render(
	<Provider store={store}>
		<ResourceMacroEditor />
	</Provider>,
	document.getElementById('resource-macro-editor')
);
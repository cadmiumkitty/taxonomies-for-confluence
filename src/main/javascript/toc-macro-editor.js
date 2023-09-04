import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';

import '@atlaskit/css-reset';
import '@atlaskit/reduced-ui-pack';

import { concept } from './reducers/Concept';
import { macro } from './reducers/Macro';
import { toc } from './reducers/TocMacro';

import { createConceptMiddleware } from './middleware/Concept';
import { createMacroMiddleware } from './middleware/Macro';
import { createTocMacroMiddleware } from './middleware/TocMacro';

import TocMacroEditor from './containers/TocMacroEditor';

const reducer = combineReducers({
	concept,
    macro,
    toc
});

const conceptMiddleware = createConceptMiddleware();
const macroMiddleware = createMacroMiddleware();
const tocMacroMiddleware = createTocMacroMiddleware();

const middleware = applyMiddleware(thunk, conceptMiddleware, tocMacroMiddleware, macroMiddleware );

const store = createStore(reducer, middleware);

ReactDOM.render(
	<Provider store={store}>
		<TocMacroEditor />
	</Provider>,
	document.getElementById('toc-macro-editor')
);
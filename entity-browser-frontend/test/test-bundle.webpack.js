global.$ = global.jQuery = require('jquery/dist/jquery');
require('angular');


require('karma-phantomjs-shim/shim.js');
require('imports?this=>window,define=>false,exports=>false,module=>false,require=>false!sinon/pkg/sinon.js');
require('imports?this=>window,define=>false,exports=>false,module=>false,require=>false!jasmine-sinon/lib/jasmine-sinon.js');
require('imports?this=>window,define=>false,exports=>false,module=>false,require=>false!jasmine-expect/dist/jasmine-matchers.js');


require('./jasmine-global');
require('angular-mocks/angular-mocks.js');

var matcherContext = require.context('./matcher', true, /.*\.js$/);
matcherContext.keys().forEach(matcherContext);


// This gets replaced by karma webpack with the updated files on rebuild
var __karmaWebpackManifest__ = [];

var testsContext = require.context('/app', true, /test\/(?!e2e\/).*\.js$/);

function inManifest(path) {
    return __karmaWebpackManifest__.indexOf(path) >= 0;
}

var runnable = testsContext.keys().filter(inManifest);

// Run all tests if we didn't find any changes
if (!runnable.length) {
    runnable = testsContext.keys();
}

runnable.forEach(testsContext);

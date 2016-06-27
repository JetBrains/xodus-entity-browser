module.exports = function(config) {
  'use strict';

  var webpackConf = require('./webpack/test.config.js');

  config.set({
    basePath: '',
    frameworks: ['jasmine', 'phantomjs-shim'],
    files: [{
      pattern: 'test/unit/test-bundle.webpack.js',
      watched: false
    }],

    exclude: [
      'app/**/e2e/**/*.js',
      'app/**/analytics/raw/**/*.js'
    ],

    preprocessors: [
      'app/vendor.js',
      'test/unit/test-bundle.webpack.js'
    ].reduce(function(preprocessors, fileMask) {
      preprocessors[fileMask] = ['webpack'];
      return preprocessors;
    }, {}),

    webpack: webpackConf,
    webpackServer: webpackConf.devServer,

    browsers: ['PhantomJS'],
    browserDisconnectTolerance: 3,
    browserNoActivityTimeout: 30000,

    reporters: ['progress'],
    reportSlowerThan: 0,

    autoWatch: true,
    singleRun: true
  });
};

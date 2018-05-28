var webpackShare = require('./webpack-share');
var webpack = require('webpack');
var SplitByPathPlugin = require('webpack-split-by-path');

webpackShare.setupFixForBuildNodeSass();

module.exports = require('webpack-config-merger')(
  {
    entry: webpackShare.entryPoints,
    output: {
      path: webpackShare.outputPath,
      filename: '[name].[hash].js'
    },
    resolve: webpackShare.resolve,
    resolveLoader: webpackShare.resolveLoader,
    module: webpackShare.module,
    bail: true,
    devtool: false,
    plugins: webpackShare.getPlugins({
      build: {
        version: process.env.VERSION_NUMBER || 'dev',
        number: process.env.BUILD_NUMBER || 'dev',
        date: (new Date()).toUTCString()
      },
      backend: {
        serviceId: '0-0-0-0-0',
        scope: ['0-0-0-0-0'],
        refresh: true
      },
      flags: {
        allowRedirects: true,
        debugAnalytics: false
      }
    }).concat([
      new webpack.DefinePlugin({
        // This has effect on the react lib size
        'process.env': {
          'NODE_ENV': '"production"'
        }
      }),
      new webpack.optimize.DedupePlugin(),
      new webpack.optimize.UglifyJsPlugin({
        minimize: true,
        sourceMap: false,
        compress: {
          warnings: false
        }
      }),
      new SplitByPathPlugin([{
        name: 'vendor',
        path: require('path').join(webpackShare.getProjectDirectory(), 'node_modules/')
      }])
    ])
  });

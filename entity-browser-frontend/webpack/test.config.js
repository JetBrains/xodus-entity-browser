var webpackShare = require('./webpack-share');
var webpack = require('webpack');

webpackShare.setupFixForBuildNodeSass();

var webpackConfig = require('webpack-config-merger')(
  {
    cache: true,
    watch: true,
    profile: true,

    devtool: 'cheap-module-source-map',
    resolve: webpackShare.resolve,
    resolveLoader: webpackShare.resolveLoader,
    module: webpackShare.module,
    plugins: webpackShare.getJavaScriptPlugins({
      build: {
        version: 'dev',
        number: 'dev',
        date: (new Date()).toUTCString()
      },
      flags: {
        allowRedirects: true,
        debugAnalytics: false
      }
    }).concat([
      new webpack.NormalModuleReplacementPlugin(/\.(gif|png|scss|css|svg)$/, 'node-noop')
    ]),
    
    devServer: {
      noInfo: false,
      stats: {
        timings: true,

        colors: false,
        chunks: false,
        hash: false,
        assets: false,
        children: false,
        version: false
      }
    }
  }
);

delete webpackConfig.entry;
webpackConfig.output = {};

module.exports = webpackConfig;

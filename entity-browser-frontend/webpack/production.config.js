var webpackShare = require('./webpack-share');
var webpack = require('webpack');
var path = require('path');

webpackShare.setupFixForBuildNodeSass();

module.exports = {
    entry: webpackShare.entryPoints,
    output: {
        path: path.resolve('./dist'),
        filename: '[name].[hash].js'
    },
    resolve: webpackShare.resolve,
    resolveLoader: webpackShare.resolveLoader,
    module: webpackShare.module,
    optimization: {
        minimize: true,
        splitChunks: {
            cacheGroups: {
                vendor: {
                    test: function(module) {
                        let userRequest = module.userRequest;
                        return typeof userRequest !== 'string' ?
                          false :
                          (/jquery/.test(userRequest) ||
                            /angular/.test(userRequest) ||
                            /react/.test(userRequest));
                    },
                    chunks: 'all',
                    name: 'vendor',
                    enforce: true
                }
            }
        }
    },
    plugins: webpackShare.getPlugins()
};

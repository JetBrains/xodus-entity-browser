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
    plugins: webpackShare.getPlugins().concat([
        new webpack.optimize.UglifyJsPlugin({
            minimize: true,
            sourceMap: false,
            compress: {
                warnings: false
            }
        }),
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendor'
        })
    ])
};

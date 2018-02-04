var path = require('path');
var webpack = require('webpack');
var projectDirectory = path.resolve(__dirname, '../');
var HtmlWebpackPlugin = require('html-webpack-plugin');

function getJavaScriptLoaders() {
    return [{
        test: /\.js/,
        include: [path.resolve(projectDirectory, 'app')],
        loaders: ['ng-annotate-loader']
    }];
}

function getStyleLoaders() {
    return [
        {
            test: /\.scss$/,
            include: [path.resolve(projectDirectory, 'app')],
            loaders: [
                'style-loader',
                'css-loader',
                'autoprefixer?{browsers:["last 2 version", "safari 5", "ie > 9", "iOS > 7", "Android > 4"]}',
                'sass-loader?outputStyle=expanded&includePaths[]=' + path.resolve('node_modules/ring-ui/components')
            ]
        }, {
            test: /\.css$/,
            include: [
                path.resolve(projectDirectory, 'app'),
                path.resolve(projectDirectory, 'node_modules/jquery-ui'),
                path.resolve(projectDirectory, 'node_modules/bootstrap'),
                path.resolve(projectDirectory, 'node_modules/bootstrap-toggle'),
                path.resolve(projectDirectory, 'node_modules/ui-select')
            ],
            loader: 'style-loader!css-loader'
        },
        {
            test: /\.woff2?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
            // Limiting the size of the woff fonts breaks font-awesome ONLY for the extract text plugin
            // loader: "url?limit=10000"
            loader: "url"
        },
        {
            test: /\.(ttf|eot|svg)(\?[\s\S]+)?$/,
            loader: 'file'
        }
    ];
}

function getImageLoaders() {
    return [{
        test: /\.png$/,
        include: path.resolve(projectDirectory, 'app'),
        loader: 'url-loader?limit=10000'
    }];
}

function getTemplateLoaders() {
    return [{
        test: /\.html$/,
        loaders: [
            'html?' + JSON.stringify({
                collapseBooleanAttributes: false,
                collapseWhitespace: false
            })
        ]
    }];
}

module.exports = {
    entryPoints: {
        main: './app/app.js'
    },

    outputPath: 'dist',

    resolve: {
        unsafeCache: true,
        alias: {
            'angular': path.resolve(projectDirectory, 'node_modules/angular'),

            //Force all components to use the same packages inspite of version in package.json
            'jquery': path.resolve(projectDirectory, 'node_modules/jquery')
        }
    },

    getJavaScriptLoaders: getJavaScriptLoaders,
    getStyleLoaders: getStyleLoaders,
    getImageLoaders: getImageLoaders,
    getTemplateLoaders: getTemplateLoaders,

    module: {
        noParse: [
            /app\/lib\/.*\.js$/
        ],
        loaders: getJavaScriptLoaders()
            .concat(getStyleLoaders())
            .concat(getImageLoaders())
            .concat(getTemplateLoaders())
    },

    getProjectDirectory: function () {
        return projectDirectory;
    },

    getHTMLPlugins: function (AppBuildConfig) {
        return [
            new HtmlWebpackPlugin({
                template: 'app/index.html',
                filename: 'index.html',
                AppBuildConfig: AppBuildConfig
            })
        ];
    },

    getJavaScriptPlugins: function (AppBuildConfig) {
        var toJson = function (data) {
            if (Object.prototype.toString.call(data) === '[object Object]') {
                return Object.keys(data).reduce(function (dist, key) {
                    dist[key] = toJson(data[key]);
                    return dist;
                }, {});
            }

            return JSON.stringify(data);
        };

        return [
            new webpack.DefinePlugin({
                AppBuildConfig: toJson(AppBuildConfig)
            }),

            new webpack.optimize.OccurenceOrderPlugin(true)
        ];
    },

    getPlugins: function (AppBuildConfig) {
        return this.getHTMLPlugins(AppBuildConfig)
            .concat(this.getJavaScriptPlugins(AppBuildConfig));
    },

    setupFixForBuildNodeSass: function () {
        //This string is REQUIRED to build with new node-sass https://github.com/jtangelder/sass-loader/issues/100
        //Without it build freezes
        process.env.UV_THREADPOOL_SIZE = 100;
    }
};

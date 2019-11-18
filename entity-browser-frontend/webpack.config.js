const path = require('path');
const url = require('url');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const config = require('./package.json').config;

module.exports = function(env, argv) {

  const isDev = isDevelopment();
  const publicPath = getPublicPath();
  const context = getAppContext();
  const apiContext = getApiContext();

  return {
    devtool: isDev ? 'eval' : 'nosources-source-map',
    mode: isDev ? 'development' : 'production',
    entry: './src/index',
    output: {
      path: path.resolve('.', './dist'),
      publicPath,
      filename: '[name].[hash].js'
    },
    plugins: [
      new webpack.HotModuleReplacementPlugin(),
      new HtmlWebpackPlugin({
        baseHref: context,
        template: path.resolve(__dirname, 'index.html')
      }),
      new webpack.DefinePlugin({
        AppBuildConfig: JSON.stringify({
          appContext: context,
          apiContext: apiContext
        })
      })
    ],
    resolve: {
      extensions: ['.js', '.jsx', '.ts', '.tsx']
    },
    devServer: {
      port: 19090,
      host: 'localhost',
      stats: {
        timings: true,

        colors: false,
        chunks: false,
        hash: false,
        assets: false,
        children: false,
        version: false
      },
      inline: true,
      overlay: true,
      disableHostCheck: true,
      contentBase: path.join(__dirname, 'src'),
      historyApiFallback: {
        index: url.resolve(context, 'index.html')
      },
      proxy: {
        [`${apiContext}/*`]: {
          target: getServerUri(),
          secure: false,
          changeOrigin: true,
          headers: {
            Connection: 'keep-alive'
          }
        }
      },
      open: true,
      openPage: isEmptyPublicPath() ? '' : config.appContext
    },
    module: {
      rules: [
        {
          test: /\.scss$/,
          use: [
            'style-loader',
            'css-loader',
            'postcss-loader',
            'sass-loader'
          ]
        },
        {
          test: /\.jsx?$/,
          use: ['babel-loader'],
          include: path.join(__dirname, 'src')
        },
        {
          test: /\.tsx?$/,
          loader: "ts-loader"
        }
      ]
    }
  };


  function isDevelopment() {
    return argv.mode !== 'production';
  }

  function isEmptyPublicPath() {
    return (isDevelopment() && process.argv.indexOf('--with-context') === -1);
  }

  function getPublicPath() {
    return isEmptyPublicPath() ? '' : addLeadingSlash(config.appContext);
  }

  function getAppContext() {
    let publicPath = getPublicPath();
    if (!publicPath) {
      return '/';
    }
    return addTrailingSlash(addLeadingSlash(publicPath));
  }

  function getApiContext() {
    return addLeadingSlash(config.apiContext);
  }

  function getServerUri() {
    const serverOptPath = process.argv.indexOf('--server') + 1;

    let argServer = null;
    if (serverOptPath > 0) {
      argServer = process.argv[serverOptPath]
    }
    const server = argServer || config.server;
    if (server in config.servers) {
      console.log('using ' + config.servers[server] + ' as a backend');
      return config.servers[server]
    }
    console.log('using ' + server + ' as a backend');
    return server;
  }

  function addLeadingSlash(str) {
    return (str && str[0] !== '/') ? `/${str}` : str;
  }

  function addTrailingSlash(str) {
    return (str && str[str.length - 1] !== '/') ? `${str}/` : str;
  }
};

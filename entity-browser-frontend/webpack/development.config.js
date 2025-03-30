var webpackShare = require('./webpack-share');
var path = require('path');
var url = require('url');
var glob = require('glob');
var parseArgs = require('minimist');
const { merge } = require('webpack-merge');
var argv = parseArgs(process.argv.slice(2), {
  default: {
    port: '19090',
    host: 'localhost',

    params: {
      example: false,
      externals: false,
      server: {}
    }
  }
});

argv.params.server = argv.params.server || {};

require('mout/object/fillIn')(argv.params.server, {
  context: '',
  hostname: "localhost",
  port: '18080',
  protocol: 'http'
});

/**
 * server context
 * This need for correct work task replace in watch mode
 * because this task called how sub process
 *
 * @return {string} The server context
 */
var getServerContext = function() {

  /**
   * @param {string} url
   * @returns {boolean} Return true if url contain trailing slash
   */
  var hasTrailingSlash = function(url) {
    return url.indexOf('/', url.length - 1) !== -1;
  };
  var serverContext = require('url').resolve('/', argv.params.server.context);
  return hasTrailingSlash(serverContext) ? serverContext : serverContext + '/';
};

webpackShare.setupFixForBuildNodeSass();

var webpackConfig = merge(
  {
    entry: require('mout/object/merge')(webpackShare.entryPoints, {
      vendor: ['node-noop']
    }),

    output: {
      publicPath: getServerContext(),
      filename: '[name].js'
    },

    resolve: webpackShare.resolve,
    resolveLoader: webpackShare.resolveLoader,
    module: webpackShare.module,
    devtool: 'eval-source-map',
    plugins: webpackShare.getPlugins(),
    devServer: {
      port: argv.port,
      host: argv.host,

      devMiddleware: {
        stats: {
          timings: true,
          colors: false,
          chunks: false,
          hash: false,
          assets: false,
          children: false,
          version: false
        }
      },

      static: {
        directory: path.join(__dirname, '../app/')
      },

      historyApiFallback: {
          index: url.resolve(getServerContext(), 'index.html')
      },

      proxy: [
        {url: '/api', timeout: 9999999},
        '/webjars'
      ].reduce(function(proxy, proxyItem) {
        var serverUri = url.format({
          hostname: argv.params.server.hostname,
          port: argv.params.server.port,
          protocol: argv.params.server.protocol
        });

        if (proxyItem.constructor === Object) {
          var proxyConfig = {
            target: serverUri,
            timeout: proxyItem.timeout
          };

          proxy[addContextToUrl(proxyItem.url)] = proxyConfig;
          proxy[addContextToUrl(proxyItem.url + '/*')] = proxyConfig;
        } else {
          proxy[addContextToUrl(proxyItem)] = serverUri;
          proxy[addContextToUrl(proxyItem + '/*')] = serverUri;
        }

        return proxy;
      }, {})
    }
  });

function addContextToUrl(url) {
  var context = getServerContext();

  if (context && context !== '/') {
    return '/' + getServerContext().replace(/\//g, '') + '/' + url.replace(/^\//, '');
  }

  return url;
}

module.exports = webpackConfig;


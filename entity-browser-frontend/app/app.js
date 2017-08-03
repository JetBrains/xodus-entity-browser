global.$ = global.jQuery = require('jquery/dist/jquery');
require('angular');

require('angular-route/angular-route');
require('angular-sanitize/angular-sanitize');
require('./styles/main.scss');
require('bootstrap');
require('angular-ui-bootstrap');
require('ui-select');

angular.module('xodus', [
    'ngRoute',
    'ui.bootstrap',
    'ui.bootstrap.tpls',
    'ui.select']
);

angular.module('xodus').config([
    '$routeProvider', '$locationProvider',
    function ($routeProvider, $locationProvider) {
        // $locationProvider.html5Mode(true);

        $routeProvider.when('/error', {
            template: require('./templates/error.html')
        }).when('/empty-store', {
            template: require('./templates/empty-store.html')
        }).otherwise({
            redirectTo: '/type/0'
        });

        function when(path, route) {
            route.resolve = {
                appState: ['$location', 'DatabaseService' ,function ($location, DatabaseService) {
                    return DatabaseService.getAppState().then(function (summary) {
                        if (!summary.current) {
                            $location.path('/setup');
                            return;
                        }
                        if (!angular.isArray(summary.current.types) || !summary.current.types.length) {
                            $location.path('/empty-store');
                        }
                    }).catch(function () {
                        $location.path('/error');
                    });
                }]
            };
            $routeProvider.when(path, route);
        }

        when('/type/:typeId', {
            template: require('./templates/main.html'),
            reloadOnSearch: false
        });
        when('/type/:typeId/entity/:entityId', {
            template: require('./templates/entity.html')
        });
        when('/type/:typeId/new', {
            template: require('./templates/entity.html')
        });
        when('/setup', {
            template: require('./templates/setup.html')
        });
    }]);

angular.module('xodus').config(['$httpProvider', function ($httpProvider) {
    if (!$httpProvider.defaults.headers.get) {
        $httpProvider.defaults.headers.get = {};
    }
    //disable IE ajax request caching
    $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Mon, 26 Jul 1997 05:00:00 GMT';
    $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
    $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';
}]);


require('./styles/main.scss');
require('./service/db');
require('./service/entity-type');
require('./service/entity');
require('./service/navigation');
require('./service/confirmation');
require('./controller/confirmation-dialog');
require('./controller/data-view');
require('./controller/search');
require('./controller/form-view/form-view');
require('./controller/form-view/blobs');
require('./controller/form-view/entity-view');
require('./controller/form-view/links');
require('./controller/form-view/properties');
require('./controller/setup/db-dialog');
require('./controller/setup/db');

require('./directive/form-view');
require('./directive/data-view');
require('./directive/entity-link');
require('./directive/store-setup');
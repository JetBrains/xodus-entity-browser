global.$ = global.jQuery = require('jquery/dist/jquery');
require('angular');

require('angular-route/angular-route');
require('angular-sanitize/angular-sanitize');
require('bootstrap');
require('angular-ui-bootstrap');
require('ui-select');
require('bootstrap-toggle/js/bootstrap-toggle.js');

require('bootstrap/dist/css/bootstrap.css');
require('ui-select/dist/select.css');
require('bootstrap-toggle/css/bootstrap-toggle.css');
require('./styles/main.scss');

require('font-awesome-loader');

angular.module('xodus', [
    'ngRoute',
    'ui.bootstrap',
    'ui.bootstrap.tpls',
    'ui.select']
);

angular.module('xodus').config([
    '$routeProvider', '$locationProvider',
    function ($routeProvider, $locationProvider) {
        $locationProvider.html5Mode(true);

        $routeProvider.when('/error', {
            template: require('./pages/error.html')
        }).when('/databases', {
            template: require('./pages/databases.html')
        }).otherwise({
            redirectTo: '/'
        });

        function when(path, route) {
            route.resolve = {
                databases: ['$location', 'databaseService', '$q',function ($location, databaseService, $q) {
                    return databaseService.getDatabases().catch(function () {
                        $location.path('/error');
                        return $q.reject();
                    });
                }]
            };
            $routeProvider.when(path, route);
        }

        when('/', {
            template: require('./pages/databases.html')
        });
        when('/databases/:databaseId', {
            template: require('./pages/database.html'),
            reloadOnSearch: false
        });
        when('/databases/:databaseId/entities/:entityId', {
            template: require('./pages/entity.html')
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
}]).config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);


require('./styles/main.scss');
require('./service/db');
require('./service/entity-type');
require('./service/entity');
require('./service/navigation');
require('./service/confirmation');
require('./service/alert');
require('./service/current-database');
require('./controller/confirmation-dialog');
require('./controller/data-view');
require('./controller/search');
require('./controller/form-view/form-view');
require('./controller/form-view/blobs');
require('./controller/form-view/entity-view');
require('./controller/form-view/links');
require('./controller/form-view/properties');
require('./controller/setup/db-dialog');
require('./controller/databases');
require('./controller/database');
require('./controller/entity');
require('./controller/alert');

require('./directive/form-view');
require('./directive/data-view');
require('./directive/entity-link');
require('./directive/search');
require('./directive/linked-entities-view');
require('./directive/toogle');
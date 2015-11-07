//angular.module('xodus', ['ngRoute', 'xodus.services', 'xodus.controllers', 'xodus.directives']);
angular.module('xodus', ['ngRoute', 'ui.bootstrap', 'ui.bootstrap.tpls']);

angular.module('xodus').config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/type/:entityId', {
                templateUrl: 'views/main.html'
            }).when('/error', {
                templateUrl: 'views/error.html'
            }).
            otherwise({
                redirectTo: '/type/0',
                templateUrl: 'views/main.html'
            });
    }]);

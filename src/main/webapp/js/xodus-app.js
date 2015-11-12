angular.module('xodus', ['ngRoute', 'ui.bootstrap', 'ui.bootstrap.tpls', 'ui.select']);

angular.module('xodus').config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/type/:typeId', {
                templateUrl: 'views/main.html'
            }).when('/error', {
                templateUrl: 'views/error.html'
            }).
            otherwise({
                redirectTo: '/type/0',
                templateUrl: 'views/main.html'
            });
    }]);

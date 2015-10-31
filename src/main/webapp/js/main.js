//angular.module('xodus', ['ngRoute', 'xodus.services', 'xodus.controllers', 'xodus.directives']);
angular.module('xodus', ['ngRoute']);

angular.module('xodus').config(['$routeProvider', function ($routeProvider) {
    $routeProvider.when('/:type', {
        templateUrl: 'views/personal-page.html'
    }).
        otherwise({
            redirectTo: '/',
            templateUrl: 'views/main.html'
        });
}]);

angular.module("xodus").controller("MainCtrl", ["$http", "$location", "$scope", function ($http, $location, $scope) {
    var main = this;
    $scope.types = null;
    $scope.selectedType = null;
    $http.get("api/types").then(function (response) {
        $scope.types = response.data;
        $scope.selectedType = response.data[0];
        if ($location.path()) {

        }
        $location.path($scope.selectedType.id);
    });
    main.onTypeSelect = function (type) {
        $scope.selectedType = type;
        $location.path(type.id);
    };

}]);


angular.module("xodus").controller("DataViewCtrl", ["$http", "$scope", function ($http, $scope) {
    var dataView = this;
    dataView.isSearchExecuted = false;
    dataView.isListView = true;
    dataView.item = true;
    dataView.searchQuery = null;
    dataView.results = [];
    dataView.hasResults = function () {
        return dataView.results.length > 0;
    }
    dataView.onSearch = function () {
        $http.get("api/type/" + $scope.selectedType.id + "/entities", {
            params: {
                q: dataView.searchQuery
            }
        }).then(function (response) {
            dataView.isSearchExecuted = true;
            dataView.results = response.data;
        });
    };
    dataView.toggleView = function () {
        dataView.isListView = !dataView.isListView;
    };
    dataView.newEntity = function () {
        dataView.toggleView();
    };
}]);

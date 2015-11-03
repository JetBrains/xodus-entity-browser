angular.module('xodus').controller('MainController', ['$http', '$location', '$scope',
    function($http, $location, $scope) {
        var main = this;
        $scope.types = null;
        $scope.selectedType = null;
        $http.get('api/types').then(function(response) {
            $scope.types = response.data;
            $scope.selectedType = response.data[0];
            if ($location.path()) {

            }
            $location.path($scope.selectedType.id);
        });
        main.onTypeSelect = function(type) {
            $scope.selectedType = type;
            $location.path(type.id);
        };
    }]);

angular.module('xodus').controller('MainController', ['EntityTypeService', '$location', '$routeParams', '$scope',
    function(types, $location, $routeParams, $scope) {
        var main = this;
        $scope.types = [];
        $scope.filteredTypes = [];
        $scope.selectedType = null;
        main.filter = false;
        main.typeName = null;
        types.all().then(function(data) {
            $scope.types = data;
            $scope.filteredTypes = data;
            updateType();
            if ($scope.types.length >= 10) {
                main.filter = true;
            }
        });
        main.onTypeSelect = function(type) {
            //$scope.selectedType = type;
            $location.path('/type/' + type.id);
        };
        main.doFilter = function() {
            if (!main.typeName) {
                $scope.filteredTypes = $scope.types;
            } else {
                var filtered = [];
                angular.forEach($scope.types, function(type) {
                    if (type.name.toLowerCase().indexOf(main.typeName.toLowerCase()) > -1) {
                        filtered.push(type);
                    }
                });
                $scope.filteredTypes = filtered;
            }
        };
        var cleanUp = $scope.$on('$routeChangeSuccess', updateType);
        $scope.$on('$destroy', cleanUp);

        function updateType() {
            if ($routeParams.typeId) {
                angular.forEach($scope.types, function(type) {
                    if (type.id === $routeParams.typeId) {
                        $scope.selectedType = type;
                    }
                });
            }
            if (!$scope.selectedType && $scope.types.length) {
                $scope.selectedType = $scope.types[0];
                $location.path('/type/' + $scope.selectedType.id);
            }
        }
    }]);

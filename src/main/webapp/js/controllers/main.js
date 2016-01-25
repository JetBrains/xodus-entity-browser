angular.module('xodus').controller('MainController', [
    'EntityTypeService',
    '$location',
    '$routeParams',
    '$scope',
    '$uibModal',
    'NavigationService',
    function (types, $location, $routeParams, $scope, $uibModal, navigation) {
        var main = this;
        $scope.types = [];
        $scope.selectedType = null;
        $scope.searchQuery = $routeParams.q;
        if (!$routeParams.typeId) {
            navigation.toType();
            return;
        }
        types.all().then(function (data) {
            $scope.types = data;
            updateType();
        });
        main.onTypeSelect = function (type) {
            navigation.toType(type.id).search('q', null);
        };
        main.onSearch = function () {
            $location.search('q', $scope.searchQuery);
        };
        main.newEntity = function () {
            navigation.toEntity($scope.selectedType.id);
        };


        $scope.openInfo = function () {
            var searchInfo = $uibModal.open({
                animation: true,
                templateUrl: 'search-info.html',
                size: 'lg',
                controller: ['$scope', function ($scope) {
                    $scope.closeInfo = function () {
                        searchInfo.dismiss();
                    };
                }]
            })
        };

        function updateType() {
            angular.forEach($scope.types, function (type) {
                if (type.id === $routeParams.typeId) {
                    $scope.selectedType = type;
                }
            });
        }
    }]);

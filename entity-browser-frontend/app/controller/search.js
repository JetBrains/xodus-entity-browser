angular.module('xodus').controller('SearchController', [
    'EntityTypeService',
    '$location',
    '$routeParams',
    '$scope',
    '$uibModal',
    'NavigationService',
    'ConfirmationService',
    function (types, $location, $routeParams, $scope, $uibModal, navigation, confirmation) {
        var ctrl = this;
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
        ctrl.onTypeSelect = function (type) {
            navigation.toType(type.id).search('q', null);
        };
        ctrl.onSearch = function () {
            $location.search('q', $scope.searchQuery);
        };
        ctrl.newEntity = function () {
            navigation.toEntity($scope.selectedType.id);
        };

        ctrl.deleteSearchResult = function () {
            types.search($routeParams.typeId, $scope.searchQuery).then(function (result) {
                confirmation({
                    label: 'You are going to delete "' + result.totalCount + '" entities',
                    message: 'Are you sure to proceed?',
                    action: 'Proceed'
                }, function () {
                    types.bulkDelete($routeParams.typeId, $scope.searchQuery).catch()
                });
            })
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

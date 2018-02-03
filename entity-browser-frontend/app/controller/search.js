angular.module('xodus').controller('SearchController', [
    'EntityTypeService',
    '$location',
    '$routeParams',
    '$scope',
    '$uibModal',
    'navigationService',
    'ConfirmationService',
    function (types, $location, $routeParams, $scope, $uibModal, navigation, confirmation) {
        var searchCtrl = this;


        searchCtrl.$onInit = function () {
            syncCtrl();
        };

        searchCtrl.onTypeSelect = syncLocation(true);
        searchCtrl.onSearch = syncLocation(false);

        searchCtrl.newEntity = function () {
            navigation.toEntity($scope.selectedType.id);
        };

        searchCtrl.deleteSearchResult = function () {
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

        function syncLocation(onlyType) {
            return function () {
                $location.search({
                    typeId: searchCtrl.selectedType.id,
                    q: onlyType ? null : searchCtrl.searchQuery
                });
            }
        }

        function syncCtrl() {
            var locationTypeId = $location.search().typeId;
            var result = null;
            if (locationTypeId) {
                result = searchCtrl.fullDatabase.types.find(function (type) {
                    return type.id === $location.search().typeId;
                }) || searchCtrl.fullDatabase.types[0];
            } else {
                result = searchCtrl.fullDatabase.types[0];
            }
            searchCtrl.selectedType = result;
            searchCtrl.searchQuery = $location.search().q;
        }
    }]);

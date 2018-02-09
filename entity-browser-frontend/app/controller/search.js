angular.module('xodus').controller('SearchController', [
    'EntityTypeService',
    '$location',
    '$routeParams',
    '$scope',
    '$uibModal',
    'navigationService',
    'ConfirmationService',
    'alert',
    function (types, $location, $routeParams, $scope, $uibModal, navigation, confirmation, alert) {
        var searchCtrl = this;

        searchCtrl.$onInit = function () {
            syncCtrl();
        };

        searchCtrl.onTypeSelect = syncLocation(true);
        searchCtrl.onSearch = syncLocation(false);

        searchCtrl.newEntity = function () {
            navigation(searchCtrl.fullDatabase).toEntity(searchCtrl.selectedType.id);
        };

        searchCtrl.deleteSearchResult = function () {
            var locationTypeId = searchCtrl.selectedType.id;
            types.search(searchCtrl.fullDatabase, locationTypeId, searchCtrl.searchQuery).then(function (result) {
                confirmation({
                    label: 'You are going to delete "' + result.totalCount + '" entities',
                    message: 'Are you sure to proceed?',
                    action: 'Proceed'
                }, function () {
                    types.bulkDelete(searchCtrl.fullDatabase, locationTypeId, searchCtrl.searchQuery)
                        .catch(alert.showHttpError)
                        .then(function () {
                            searchCtrl.onSearch();
                        });
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
                locationTypeId = locationTypeId.toString();
                result = searchCtrl.fullDatabase.types.find(function (type) {
                    return type.id === parseInt(locationTypeId);
                }) || searchCtrl.fullDatabase.types[0];
            } else {
                result = searchCtrl.fullDatabase.types[0];
            }
            searchCtrl.selectedType = result;
            searchCtrl.searchQuery = $location.search().q;
        }
    }]);

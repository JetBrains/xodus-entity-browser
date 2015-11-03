angular.module('xodus').controller('DataViewController', ['EntityTypesService', '$http', '$scope', '$uibModal',
    function(types, $http, $scope, $uibModal) {
        var dataView = this;
        $scope.$watch('selectedType()', reset);
        reset();
        dataView.hasResults = function() {
            return dataView.results.length > 0;
        };
        dataView.onSearch = function() {
            $http.get('api/type/' + $scope.selectedType().id + '/entities', {
                params: {
                    q: dataView.searchQuery
                }
            }).then(function(response) {
                dataView.isSearchExecuted = true;
                dataView.results = response.data;
            });
        };
        dataView.toggleView = function() {
            dataView.isListView = !dataView.isListView;
        };
        dataView.newEntity = function() {
            dataView.toggleView();
        };

        dataView.edit = function(item) {
            dataView.toggleView();
        };

        dataView.deleteItem = function(item) {
            $uibModal.open({
                animation: true,
                templateUrl: 'views/delete-confirmation.html',
                controller: 'ConfirmationController',
                resolve: {
                    item: function() {
                        return item;
                    }
                }
            }).result.then(function(result) {
                    if (result) {
                        console.log('item deleted');
                    }
                });
        };

        function reset() {
            dataView.isSearchExecuted = false;
            dataView.isListView = true;
            dataView.item = true;
            dataView.searchQuery = null;
            dataView.results = [];
            $scope.type = $scope.selectedType();
        }
    }]);


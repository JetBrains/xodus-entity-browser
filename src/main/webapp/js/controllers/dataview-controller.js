angular.module('xodus').controller('DataViewController', ['EntityTypeService', '$http', '$scope', '$uibModal',
    function(types, $http, $scope, $uibModal) {
        var dataView = this;
        $scope.$watch('selectedType()', reset);
        reset();
        dataView.onSearch = function() {
            dataView.pager = newPager(dataView.searchQuery);
            dataView.pager.pageChanged(1);
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
                templateUrl: 'views/directives/delete-confirmation.html',
                controller: 'ConfirmationController',
                resolve: {
                    item: function() {
                        return item;
                    }
                }
            }).result.then(function(result) {
                    if (result) {
                        //console.log('item deleted');
                    }
                });
        };

        function reset() {
            dataView.isSearchExecuted = false;
            dataView.isListView = true;
            dataView.searchQuery = null;
            dataView.pageSize = 50;
            $scope.type = $scope.selectedType();
            dataView.pager = newPager(null);

            //uncomment this if you want to load data on view show
            //dataView.pager.pageChanged(1);
        }

        function newPager(searchTerm) {
            return {
                totalCount: 0,
                items: [],
                currentPage: 1,
                pageChanged: function() {
                    var pageNo = this.currentPage;
                    var offset = (pageNo - 1) * dataView.pageSize;
                    var self = this;
                    self.currentPage = pageNo;
                    types.search($scope.selectedType().id, searchTerm, offset).then(function(data) {
                        self.items = data.items;
                        self.totalCount = data.totalCount;
                        dataView.isSearchExecuted = true;
                    });
                },
                hasPagination: function() {
                    return this.totalCount > dataView.pageSize;
                },
                hasResults: function() {
                    return this.items.length > 0;
                }
            };
        }
    }]);


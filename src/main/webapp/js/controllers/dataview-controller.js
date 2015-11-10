angular.module('xodus').controller('DataViewController', ['EntityTypeService', 'EntitiesService', '$scope', '$uibModal',
    function(types, entities, $scope, $uibModal) {
        var dataView = this;
        dataView.isSearchExecuted = false;
        dataView.isListView = true;

        dataView.searchQuery = null;
        dataView.pageSize = 50;
        $scope.type = $scope.selectedType();
        dataView.pager = newPager(null);
        dataView.newInstance = entities.newEntity($scope.type.id, $scope.type.name);
        dataView.currentEntity = angular.copy(dataView.newInstance);

        //uncomment this if you want to load data on view show
        //dataView.pager.pageChanged(1);

        dataView.onSearch = function() {
            dataView.pager = newPager(dataView.searchQuery);
            dataView.pager.pageChanged();
        };

        dataView.toggleView = toggleView;
        dataView.newEntity = function() {
            toggleView();
            dataView.currentEntity = angular.copy(dataView.newInstance);
        };

        dataView.edit = function(item) {
            toggleView();
            dataView.currentEntity = angular.copy(item);
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
                        types.remove(item.typeId, item.id).success(dataView.onSearch).fail(function() {
                        });
                    }
                });
        };

        dataView.refresh = function() {
            dataView.toggleView();
            dataView.pager.pageChanged();
        };

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

        function toggleView() {
            dataView.isListView = !dataView.isListView;
        }
    }]);

